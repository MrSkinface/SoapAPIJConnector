package org.exite;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.*;

import org.apache.http.HttpHost;
import org.apache.log4j.Logger;
import org.exite.RestExAPI.IRestExAPI;
import org.exite.RestExAPI.RestExAPI;
import org.exite.RestExAPI.RestExAPIEcxeption;
import org.exite.SoapExAPI.ISoapExAPI;
import org.exite.SoapExAPI.SoapExAPI;
import org.exite.SoapExAPI.SoapExAPIException;
import org.exite.crypt.CryptEx;
import org.exite.crypt.ECertificate;
import org.exite.crypt.ESignType;
import org.exite.exception.DuplicateDocException;
import org.exite.exception.NoDocFoundException;
import org.exite.obj.Config;
import org.exite.obj.Entity;
import org.exite.workers.queues.QRecord;

public class Controller {

	private static final Logger log=Logger.getLogger(Controller.class);
	
	private Config conf;

    private CryptEx cryptex;
	
	private IRestExAPI api;
	private String authToken;
	
	private ISoapExAPI soapEsf;
	private ISoapExAPI soapInvoice;
	
	public Controller(Config conf) throws Exception {

		this.conf=conf;		
		setupApi();
        cryptex = new CryptEx(conf.getCertStorePass());
	}

    public List<String>getList(String filter){
        return getList(filter, null);
    }

	public List<String>getList(String filter, String extension) {
		List<String>list=new LinkedList<>();
		try {
			for (String string : soapEsf.getList()) 
				if(string.contains(filter)){
					if(extension != null){
						if(string.endsWith(extension)){
							list.add(string);
						}
					} else {
						list.add(string);
					}
				}
		} catch (SoapExAPIException e) {
			log.error(e.getMessage());
		}
		return list;
	}

	public List<String>getList(Set<String> filterSet, String extension) {
		List<String>list=new LinkedList<>();
		try {
            for(String filter : filterSet){
                list.addAll(getList(filter, extension));
            }
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return list;
	}

	public List<String>getList(String[]filters, String extension) {
		return getList(new HashSet<>(Arrays.asList(filters)), extension);
	}

	public boolean removeSoapDoc(String fileName) {
		try {
			soapEsf.archiveDoc(fileName);
			log.info("["+fileName+"] removed .");
			return true;
		} catch (SoapExAPIException e) {
			log.error(e.getMessage());
			return false;
		}
	}

    public void removeSoapDoc(String fileName, boolean removewithSign) {
        removeSoapDoc(fileName);
        if(fileName.endsWith(".xml") && removewithSign){
            removeSoapDoc(fileName.replace(".xml",".bin"));
        }
    }

	public boolean sendSoapDoc(String fileName, byte[]content) {
		try {
			soapInvoice.sendDoc(fileName, content);
			log.info("["+fileName+"] send .");
			return true;
		} catch (SoapExAPIException e) {
			log.error(e.getMessage());
			return false;
		}
	}

	public byte[]getDoc(String fileName) {
		try {
			return soapEsf.getDoc(fileName);
		} catch (SoapExAPIException e) {
			log.error(e.getMessage());
			return null;
		}
	}

    public byte[]getDocContent(String uuid){
	    try{
	        return getDocContent_(uuid);
        } catch (RestExAPIEcxeption e){
            log.error(e.getMessage());
            if(e.getMessage().contains("Not authorized")){
                log.warn("Try to re-authorize");
                setupApi();
                try{
                    return getDocContent_(uuid);
                } catch (RestExAPIEcxeption e1){
                    log.error(e.getMessage());
                    return null;
                }
            }
            return null;
        }
    }

	private byte[]getDocContent_(String uuid) throws RestExAPIEcxeption {
        Entity entity=api.getContent(authToken, uuid);
        byte[] body = Base64.getDecoder().decode(entity.body);
        return body;
	}

    public String getBase64TicketBody(String docUUID) throws DuplicateDocException, NoDocFoundException, RestExAPIEcxeption {
        return getBase64TicketBody(docUUID, null);
    }

	public String getBase64TicketBody(String docUUID, String rejectComment) throws DuplicateDocException, NoDocFoundException, RestExAPIEcxeption {
        try{
            if(rejectComment == null){
                return api.generateTicket(authToken,
                        docUUID,
                        conf.cryptex.signer.signer_name,
                        conf.cryptex.signer.signer_surName,
                        conf.cryptex.signer.signer_orgUnit,
                        conf.cryptex.signer.signer_org_inn);
            } else {
                return api.generateReject(authToken,
                        docUUID,
                        conf.cryptex.signer.signer_name,
                        conf.cryptex.signer.signer_surName,
                        conf.cryptex.signer.signer_orgUnit,
                        conf.cryptex.signer.signer_org_inn,
                        rejectComment);
            }
        } catch (RestExAPIEcxeption e) {
            log.error(e.getMessage()+" doc uuid [" + docUUID + "]");
            if(e.getMessage().contains("already queued")) {
                throw new DuplicateDocException(e.getMessage(), e);
            } else if (e.getMessage().contains("No document found")){
                throw new NoDocFoundException(e.getMessage(), e);
            } else {
                throw e;
            }
        }
	}

    public String getBase64TicketSign(String Base64TicketBody){
        return getStringSignBody(Base64TicketBody);
    }


	public boolean confirmEdoDoc(QRecord record) throws DuplicateDocException, NoDocFoundException, RestExAPIEcxeption {
		try {
			api.sendTicket(authToken, record.getUUID(), record.getBase64ticketBody(), record.getBase64ticketSign());
			log.info("["+record.getUUID()+"] confirmed .");
			return true;
		} catch (RestExAPIEcxeption e) {
			log.error(e.getMessage() + " doc uuid [" + record.getUUID() + "]");
			if(e.getMessage().contains("already queued")) {
				throw new DuplicateDocException(e.getMessage(), e);
			} else if (e.getMessage().contains("No document found")){
                throw new NoDocFoundException(e.getMessage(), e);
			} else if(e.getMessage().contains("Not authorized")){
                log.warn("Try to re-authorize");
                setupApi();
                api.sendTicket(authToken, record.getUUID(), record.getBase64ticketBody(), record.getBase64ticketSign());
                log.info("["+record.getUUID()+"] confirmed .");
                return true;
            } else {
                throw e;
            }
		}		
	}

	private String getStringSignBody(String docBody) {
		byte[] doc;
		String sign=null;
		try {
			doc = Base64.getDecoder().decode(docBody);
			sign = Base64.getEncoder().encodeToString(getSign(doc));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return sign;
	}

    public byte[] getSign(final byte[] body) throws Exception {
		return cryptex.signCAdES(body, conf.cryptex.alias, ESignType.DER);
    }

    public List<String> getAliases() throws Exception {
        return cryptex.getAliases();
    }

    public ECertificate getCert(String alias) throws Exception {
        return cryptex.getCertificate(alias);
    }

    public void usageHelp(){
        System.out.println("\tInfo: ");
        System.out.println("\tthere are several helping options");
        System.out.println("\t1) [-testconnection]\tshows if there everything is O.K. with connecting to provider's services");
        System.out.println("\t2) [-testcrypto]\tshows all certificates in JCP storage and test them");
    }

    public void testCrypto() throws Exception {
        System.out.println("Aliases list: ");
        for (String string : getAliases()){
            System.out.println(string);
            System.out.println("info:");
            ECertificate cert = getCert(string);
            System.out.println("valid from / to: " + cert.getValidityFrom()+" - "+cert.getValidityTo());
            System.out.println("toString: " + cert.toString());
            cryptex.signCAdES("test".getBytes(), string, ESignType.DER);
            System.out.println("sign O.K.");
        }
    }

    public void testConnection() throws Exception {
        try {
            if(this.authToken != null){
                System.out.println("https is O.K.");
            }
            soapEsf.getList();
            soapInvoice.getList();
            System.out.println("soap is O.K.");
        } catch (SoapExAPIException e) {
            log.error(e);
        }
    }

    public void checkCryptexAlive(){
        try{
            getSign("test".getBytes());
        } catch (Exception e){
            log.error("[FATAL ERROR] Cryptex fails :", e);
            System.exit(500);
        }
    }

	public void setupApi() {
		try {
			this.api=conf.proxy!=null?new RestExAPI(new HttpHost(conf.proxy.host, conf.proxy.port)):new RestExAPI();
			this.authToken=api.authorize(conf.rest.get(0).login, conf.rest.get(0).password);
			this.soapEsf=new SoapExAPI(conf.soap.get(0).login,conf.soap.get(0).password);
			this.soapInvoice=new SoapExAPI(conf.soap.get(1).login,conf.soap.get(1).password);
		} catch (MalformedURLException e) {
			log.error(e);
		} catch (RestExAPIEcxeption e) {
            log.error(e);
		}		
	}
}
