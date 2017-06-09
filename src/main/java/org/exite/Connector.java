package org.exite;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.exite.obj.Config;
import org.exite.obj.EventListMode;
import org.exite.obj.SystemStatus;
import org.exite.utils.Parser;

public class Connector 
{
	private static final Logger log=Logger.getLogger(Connector.class);
	
	private Config conf;
	private Controller controller;
	
	private String timeFrom;
	private String timeTo;
	
	private Map<String,String>docTypes;
	
	public Connector() 
	{		
		
		do 
		{
			conf=getConfig();
			log.info("start");
			conf.cryptex.signer.checkFromCertSigner(conf.cryptex);		
			controller=new Controller(conf);
			/**/
			timeFrom=LocalDateTime.now().minusHours(conf.rest.get(0).fromMinus).format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss"));
			timeTo=LocalDateTime.now().plusHours(conf.rest.get(0).toPlus).format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss"));
			/**/
			
			/**/
			docTypes=new HashMap<String,String>();			
			docTypes.put("ON_SCHFDOPPR", "upd_");
			docTypes.put("ON_KORSCHFDOPPR", "ukd_");
			docTypes.put("DP_PDOTPR", null);
			/**/
			
			try 
			{
				handleDocs();
				if(conf.tickets.confirm.equals("status"))
					handleStatuses();
			} catch (Exception e) 
			{
				log.info(e);
			}
			log.info("end");
			try 
			{
				Thread.sleep(conf.daemon.value*1000);
			} catch (Exception e) 
			{
				log.info(e);
			}
			
		} while (conf.daemon.enabled);	
	}	
	
	private void handleDocs()
	{
		killSpam("DP_IZVPOL_");
		
		List<String>files=new LinkedList<String>();
		if(conf.tickets.mode.equals("soap"))
			files=controller.getList(docTypes.keySet());
		else if(conf.tickets.mode.equals("rest"))
			files=controller.getEventUUIDList(timeFrom, timeTo, EventListMode.ESF_UPD, docTypes.keySet());
		for (String uuid : files) 
		{
			String docType=docTypes.get(controller.getEventType(uuid));
			if(docType!=null)
			{
				byte[]docBody=controller.getDocContent(uuid);
				String fileName=docType+uuid+".xml";
				if(controller.sendSoapDoc(fileName, docBody))
				{
					/* ??? */
					if(!conf.tickets.confirm.equals("auto"))
					{
						for (String name : controller.getList(uuid))
							controller.removeSoapDoc(name);
						continue;
					}						
					if(controller.confirmEdoDoc(uuid))
					{
						for (String name : controller.getList(uuid))
							controller.removeSoapDoc(name);						
					}
				}
			}else
			{
				if(controller.confirmEdoDoc(uuid))
				{
					for (String name : controller.getList(uuid))
						controller.removeSoapDoc(name);						
				}
			}
		}	
	}
	private void handleStatuses() throws Exception 
	{
		//throw new Exception("not implemented yet");
		for (String fileName : controller.getList("EDOSTATUS_")) 
		{
			SystemStatus status=(SystemStatus)Parser.fromXml(controller.getDoc(fileName), SystemStatus.class);
			//System.out.println(status);
			if(status.STATUSCODE.equals("ERROR"))
			{
				controller.rejectEdoDoc(status.DOCID,status.COMMENT);			
				
			}
			else
			{
				controller.confirmEdoDoc(status.DOCID);
				controller.confirmUpdUkdTitul(status.DOCID);
			}			
			controller.removeSoapDoc(fileName);
		}
	}
	
	private void killSpam(String spam)
	{
		for (String fileName : controller.getList(spam)) 
			controller.removeSoapDoc(fileName);
	}
	

	public static void main(String[] args) throws Exception
	{		
		new Connector();

	}
	private Config getConfig()
	{		
		try 
		{
			PropertyConfigurator.configure(new FileInputStream(Paths.get(System.getProperty("user.dir")).resolve("config").resolve("log4j.properties").toString()));			
			Config conf=(Config)Parser.fromXml(Files.readAllBytes(Paths.get(System.getProperty("user.dir")).resolve("config").resolve("config.xml")), Config.class);
			return conf;
		} catch (Exception e) 
		{
			log.info(e);
		}
		return null;
	}
}
