
package org.exite;

import java.io.FileInputStream;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.exite.obj.Config;
import org.exite.obj.EventListMode;

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
		conf=getConfig();
		log.info("start");
		conf.cryptex.signer.checkFromCertSigner(conf.cryptex);		
		controller=new Controller(conf);
		/**/
		timeFrom=LocalDateTime.now().minusDays(2).format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss"));
		timeTo=LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss"));
		/**/
		
		/**/
		docTypes=new HashMap<String,String>();		
		if(LocalDateTime.now().isBefore(LocalDateTime.of(2017, 07, 01, 00, 00)))
		{
			docTypes.put("ON_SFAKT", "sfakt_");
			docTypes.put("ON_KORSFAKT", "korsfakt_");			
		}
		docTypes.put("ON_SCHFDOPPR", "upd_");
		docTypes.put("ON_KORSCHFDOPPR", "ukd_");
		docTypes.put("DP_PDOTPR", null);
		/**/
		
		System.out.println(conf.toString());
		handleDocs();
		
		
		log.info("end");
	}
	private void handleDocs()
	{
		for (String fileName : controller.getList("DP_IZVPOL_")) 
			controller.removeSoapDoc(fileName);
		for (String uuid : controller.getEventUUIDList(timeFrom, timeTo, EventListMode.ESF_UPD, docTypes.keySet()))
			if(controller.confirmEdoDoc(uuid))
			{
				for (String fileName : controller.getList(uuid))
					controller.removeSoapDoc(fileName);
				String docType=docTypes.get(controller.getEventType(uuid));
				if(docType!=null)
				{
					/*if(docType=="upd_" || docType=="ukd_")
						controller.confirmUpdUkdTitul(uuid);*/
					byte[]docBody=controller.getDocContent(uuid);
					String fileName=docType+uuid+".xml";
					controller.sendSoapDoc(fileName, docBody);
				}
			}
	}	
	

	public static void main(String[] args) throws Exception
	{		
		new Connector();

	}
	private Config getConfig()
	{
		JAXBContext jc;
		try 
		{
			PropertyConfigurator.configure(new FileInputStream(Paths.get(System.getProperty("user.dir")).resolve("config").resolve("log4j.properties").toString()));
			jc = JAXBContext.newInstance(Config.class);
			Unmarshaller unm=jc.createUnmarshaller();
			Config conf=(Config)unm.unmarshal(new FileInputStream(Paths.get(System.getProperty("user.dir")).resolve("config").resolve("config.xml").toString()));
			return conf;
		} catch (Exception e) 
		{
			log.error(e);
		}
		return null;
	}
}
