package org.exite;

import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.options.Options;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.exite.RestExAPI.IRestExAPI;
import org.exite.RestExAPI.RestExAPI;
import org.exite.RestExAPI.RestExAPIEcxeption;
import org.exite.SoapExAPI.ISoapExAPI;
import org.exite.SoapExAPI.SoapExAPI;
import org.exite.SoapExAPI.SoapExAPIException;
import org.exite.cryptex.client.CryptoClient;
import org.exite.cryptex.objects.ECertificate;
import org.exite.obj.Config;
import org.exite.obj.SystemStatus;
import org.exite.utils.Parser;

public class Connector implements Runnable {

	private static final Logger log=Logger.getLogger(Connector.class);
	
	private Config conf;
	private Controller controller;

	private Map<String,String>docTypes;

	private boolean execute = false;
	private long sleepTime = 1;
	
	public Connector() {
		registerShutdownHook();
		new Thread(this).start();
	}

	public Connector(String option) {
		switch(option){
			case "-help":
				usageHelp();
				break;
			case "-testconnection":
				testConnection();
				break;
			case "-testcrypto":
				testCrypto();
				break;
			default:
				System.out.println("wrong usage . See [-help] option");
				break;
		}
	}

	private void usageHelp(){
		System.out.println("\tInfo: ");
		System.out.println("\tthere are several helping options");
		System.out.println("\t1) [-testconnection]\tshows if there everything is O.K. with connecting to provider's services");
		System.out.println("\t2) [-testcrypto]\tshows all certificates in JCP storage and test them");
	}

	private void testConnection(){
		Config conf=getConfig();
		IRestExAPI api;
		ISoapExAPI soapEsf;
		ISoapExAPI soapInvoice;
		api = new RestExAPI();
		String auth = null;
		try {
			auth = api.authorize(conf.rest.get(0).login, conf.rest.get(0).password);
			if(auth!=null)
				System.out.println("https is O.K.");
			soapEsf = new SoapExAPI(conf.soap.get(0).login,conf.soap.get(0).password);
			soapEsf.getList();
			soapInvoice = new SoapExAPI(conf.soap.get(1).login,conf.soap.get(1).password);
			soapInvoice.getList();
			System.out.println("soap is O.K.");
		} catch (RestExAPIEcxeption restExAPIEcxeption) {
			System.err.println("https fails, reason: " + restExAPIEcxeption.getMessage());
		} catch (MalformedURLException e) {
			System.err.println("soap fails, reason: " + e.getMessage());
		} catch (SoapExAPIException e) {
			System.err.println("soap fails, reason: " + e.getMessage());
		}
	}

	private void testCrypto(){
		List<String>aliases= null;
		try {
			aliases = CryptoClient.aliases(null);
			System.out.println("Aliases list: ");
			if(aliases.size()!=0){
				for (String string : aliases){
					System.out.println(string);
					System.out.println("info:");
					ECertificate[]certs=CryptoClient.getCertificates(null, string);
					for (ECertificate c : certs){
						System.out.println("valid from / to: " + c.getValidityFrom()+" - "+c.getValidityTo());
						System.out.println("toString: " + c.toString());
					}
				}
			} else {
				System.out.println("JCP keystorage is empty");
			}
		} catch (Exception e) {
			System.err.println("crypto fails, reason: " + e.getMessage());
		}
	}

	@Override
	public void run() {
		do{
			try{
				conf=getConfig();

				execute = Boolean.valueOf(conf.daemon.enabled);
				sleepTime = Long.valueOf(conf.daemon.value*1000);
				Options.refresh();

				log.info("start");
				conf.cryptex.signer.checkFromCertSigner(conf.cryptex);
				controller=new Controller(conf);
				docTypes=new HashMap<String,String>();
				docTypes.put("ON_SCHFDOPPR", "upd_");
				docTypes.put("ON_KORSCHFDOPPR", "ukd_");
				docTypes.put("DP_PDOTPR", null);

				handleDocs();

				if(conf.tickets.confirm.equals("status"))
					handleStatuses();
				log.info("end. Sleeping ["+sleepTime/1000+" sec] ...");

				Unirest.shutdown();
				Thread.sleep(sleepTime);
			} catch (Exception e)
			{
				log.info(e);
				execute = false;
			}
		}while(execute);
	}

	private void registerShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				execute = false;
				log.info("Received EXIT_SIGNAL");
			}
		});
	}
	
	private void handleDocs() {

		killSpam("DP_IZVPOL_");
		
		List<String>files=new LinkedList<String>();
		if(conf.tickets.mode.equals("soap"))
			files=controller.getList(docTypes.keySet());
		for (String uuid : files) {
			String docType=docTypes.get(controller.getEventType(uuid));
			if(docType!=null) {
				byte[]docBody=controller.getDocContent(uuid);
				String fileName=docType+uuid+".xml";
				if(controller.sendSoapDoc(fileName, docBody)) {
					/* ??? */
					if(!conf.tickets.confirm.equals("auto")) {
						for (String name : controller.getList(uuid))
							controller.removeSoapDoc(name);
						continue;
					}						
					if(controller.confirmEdoDoc(uuid)) {
						for (String name : controller.getList(uuid))
							controller.removeSoapDoc(name);						
					}
				}
			}else {
				if(controller.confirmEdoDoc(uuid)) {
					for (String name : controller.getList(uuid))
						controller.removeSoapDoc(name);						
				}
			}
		}	
	}
	private void handleStatuses() throws Exception {
		for (String fileName : controller.getList("EDOSTATUS_")) {
			SystemStatus status=(SystemStatus)Parser.fromXml(controller.getDoc(fileName), SystemStatus.class);
			if(status.STATUSCODE.equals("ERROR")) {
				controller.rejectEdoDoc(status.DOCID,status.COMMENT);
			} else {
				controller.confirmEdoDoc(status.DOCID);
				controller.confirmUpdUkdTitul(status.DOCID);
			}			
			controller.removeSoapDoc(fileName);
		}
	}
	
	private void killSpam(String spam) {
		for (String fileName : controller.getList(spam)) 
			controller.removeSoapDoc(fileName);
	}
	

	public static void main(String[] args) throws Exception {
		if(args.length>0)
			new Connector(args[0]);
		else
			new Connector();
	}

	private Config getConfig() {
		try {
			PropertyConfigurator.configure(new FileInputStream(Paths.get(System.getProperty("user.dir")).resolve("config").resolve("log4j.properties").toString()));			
			Config conf=(Config)Parser.fromXml(Files.readAllBytes(Paths.get(System.getProperty("user.dir")).resolve("config").resolve("config.xml")), Config.class);
			return conf;
		} catch (Exception e) {
			log.info(e);
		}
		return null;
	}
}
