package org.exite;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpHost;
import org.apache.log4j.Logger;
import org.exite.RestExAPI.IRestExAPI;
import org.exite.RestExAPI.RestExAPI;
import org.exite.RestExAPI.RestExAPIEcxeption;
import org.exite.SoapExAPI.ISoapExAPI;
import org.exite.SoapExAPI.SoapExAPI;
import org.exite.SoapExAPI.SoapExAPIException;
import org.exite.cryptex.client.CryptoClient;
import org.exite.obj.Config;
import org.exite.obj.Entity;
import org.exite.obj.Event;
import org.exite.obj.EventListMode;

public class Controller 
{
	private static final Logger log=Logger.getLogger(Controller.class);
	
	private Config conf;
	
	private IRestExAPI api;
	private String authToken;
	
	private ISoapExAPI soapEsf;
	private ISoapExAPI soapInvoice;
	
	public Controller(Config conf) 
	{
		this.conf=conf;		
		setupApi();
	}
	
	public List<String>getList(String filter)
	{
		List<String>list=new LinkedList<String>();
		try 
		{
			for (String string : soapEsf.getList()) 
				if(string.contains(filter))
					list.add(string);			
		} catch (SoapExAPIException e) 
		{			
			log.error(e.getMessage());
		}
		return list;
	}
	public boolean removeSoapDoc(String fileName)
	{
		try 
		{
			soapEsf.archiveDoc(fileName);
			log.info("["+fileName+"] removed .");
			return true;
		} catch (SoapExAPIException e) 
		{		
			log.error(e.getMessage());
			return false;
		}
	}
	public boolean sendSoapDoc(String fileName,byte[]content)
	{
		try 
		{
			soapInvoice.sendDoc(fileName, content);
			log.info("["+fileName+"] send .");
			return true;
		} catch (SoapExAPIException e) 
		{		
			log.error(e.getMessage());
			return false;
		}
	}
	public byte[]getDocContent(String uuid)
	{
		byte[]body;
		try 
		{
			Entity entity=api.getContent(authToken, uuid);
			body = Base64.getDecoder().decode(entity.body);
			return body;
		} catch (RestExAPIEcxeption e) 
		{			
			log.error(e.getMessage());
			return null;
		}		
	}
	public boolean confirmEdoDoc(String docId)
	{
		try 
		{
			String ticket=api.generateTicket(authToken, docId, conf.cryptex.signer.signer_name, conf.cryptex.signer.signer_surName, conf.cryptex.signer.signer_orgUnit, conf.cryptex.signer.signer_org_inn);
			api.sendTicket(authToken, docId, ticket, getStringSignBody(ticket));
			log.info("["+docId+"] confirmed .");
			return true;
		} catch (RestExAPIEcxeption e) 
		{
			log.error(e.getMessage()+" doc uuid ["+docId+"]");
			return false;
		}		
	}
	public boolean confirmUpdUkdTitul(String docId)
	{
		try {
			String titul=api.generateUPDAnswer(authToken, 
					docId, 
					conf.cryptex.signer.signer_name, 
					conf.cryptex.signer.signer_surName, 
					conf.cryptex.signer.signer_orgUnit, 
					conf.cryptex.signer.signer_org_inn, 
					"Товары переданы", 
					"Должностные обязанности", 
					3, 
					2);
			api.sendTicket(authToken, docId, titul, getStringSignBody(titul));
			log.info("["+docId+"] confirmed (titul) .");
			return true;
		} catch (RestExAPIEcxeption e) 
		{
			log.error(e.getMessage());
			return false;
		}		
	}
	private String getStringSignBody(String docBody)
	{
		byte[] doc;
		String sign=null;
		try 
		{
			doc = Base64.getDecoder().decode(docBody);
			sign=new String(CryptoClient.sign(conf.cryptex.password, conf.cryptex.alias, doc)).replaceAll("\\s*[\\r\\n]+\\s*", "").trim();
		} catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
		} catch (Exception e) 
		{
			e.printStackTrace();
		}		
		return sign;
	}
	public List<String>getEventUUIDList(String timeFrom, String timeTo, String filter)
	{
		List<String>list=new LinkedList<String>();
		try 
		{
			for (Event e : api.getEvents(authToken, timeFrom, timeTo))
				if(api.getDocInfo(authToken, e.document_id).doc_type.equals(filter))
					if(e.need_reply_reciept)
						list.add(e.document_id);
		} catch (RestExAPIEcxeption e) 
		{
			log.error(e.getMessage());
		}
		return list;
	}
	public List<String>getEventUUIDList(String timeFrom, String timeTo,EventListMode mode, String filter)
	{
		List<String>list=new LinkedList<String>();
		try 
		{
			for (Event e : api.getEvents(authToken, timeFrom, timeTo, mode))
				if(!e.event_status.contains("ERROR"))
					if(api.getDocInfo(authToken, e.document_id).doc_type.equals(filter))
						if(e.need_reply_reciept)
							list.add(e.document_id);
		} catch (RestExAPIEcxeption e) 
		{
			log.error(e.getMessage());
		}
		return list;
	}
	public List<String>getEventUUIDList(String timeFrom, String timeTo, Set<String> filterSet)
	{
		List<String>list=new LinkedList<String>();
		try 
		{
			for (Event e : api.getEvents(authToken, timeFrom, timeTo))
				if(!e.event_status.contains("ERROR"))
					if(filterSet.contains(api.getDocInfo(authToken, e.document_id).doc_type))
						if(e.need_reply_reciept && e.event_status.contains("RECIEVED"))
							list.add(e.document_id);
		} catch (RestExAPIEcxeption e) 
		{
			log.error(e.getMessage());
		}
		return list;
	}
	public List<String>getEventUUIDList(String timeFrom, String timeTo, EventListMode mode, Set<String> filterSet)
	{
		List<String>list=new LinkedList<String>();
		try 
		{
			for (Event e : api.getEvents(authToken, timeFrom, timeTo, mode))
				if(!e.event_status.contains("ERROR"))
					if(filterSet.contains(api.getDocInfo(authToken, e.document_id).doc_type))
						if(e.need_reply_reciept && e.event_status.contains("RECIEVED"))
							list.add(e.document_id);
		} catch (RestExAPIEcxeption e) 
		{
			log.error(e.getMessage());
		}
		return list;
	}
	public String getEventType(String uuid)
	{
		try 
		{
			return api.getDocInfo(authToken, uuid).doc_type;
		} catch (RestExAPIEcxeption e) 
		{			
			log.error(e.getMessage());
		}
		return null;
	}
	
	
	private void setupApi()
	{		
		try 
		{
			this.api=conf.proxy!=null?new RestExAPI(new HttpHost(conf.proxy.host, conf.proxy.port)):new RestExAPI();
			this.authToken=api.authorize(conf.rest.get(0).login, conf.rest.get(0).password);
			this.soapEsf=new SoapExAPI(conf.soap.get(0).login,conf.soap.get(0).password);
			this.soapInvoice=new SoapExAPI(conf.soap.get(1).login,conf.soap.get(1).password);
		} catch (MalformedURLException e) 
		{
			log.error(e.getMessage());
		} catch (RestExAPIEcxeption e) 
		{			
			log.error(e.getMessage());
		}		
	}
}
