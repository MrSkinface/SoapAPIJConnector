package org.exite;

import java.io.UnsupportedEncodingException;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.exite.crypt.CryptEx;
import org.exite.crypt.ECertificate;
import org.exite.crypt.ESignType;
import org.exite.edi.soap.*;
import org.exite.exception.DuplicateDocException;
import org.exite.exception.NoDocFoundException;
import org.exite.exception.RestException;
import org.exite.exception.SoapException;
import org.exite.objects.config.Config;
import org.exite.objects.rest.Entity;
import org.exite.rest.ExiteRestAPI;
import org.exite.rest.RestAPI;
import org.exite.workers.queues.QRecord;

import javax.xml.ws.Service;

@Slf4j
public class Controller {
	
	private Config conf;
    private CryptEx cryptex;
	
	private RestAPI api;
	private String authToken;

    private ObjectFactory factory;

    private EdiLogin updUser;
    private EdiLogin invoiceUser;
	
	private ExiteWs updSoapService;
	private ExiteWs invoiceSoapService;
	
	public Controller(Config conf) throws Exception {
        factory = new ObjectFactory();
        Service srv = new ExiteWsService();
        this.updSoapService = srv.getPort(ExiteWs.class);
        this.invoiceSoapService = srv.getPort(ExiteWs.class);
		this.conf=conf;		
		setupApi();
        cryptex = new CryptEx(conf.getCertStorePass());
	}

    public List<String>getList() throws SoapException {
	    return getList(updSoapService, updUser);
    }

    public List<String>getList(final ExiteWs service, final EdiLogin user) throws SoapException {
        GetListRequest request = factory.createGetListRequest();
        request.setUser(user);
        GetListResponse response = service.getList(request);
        EdiFileList list = response.getResult();
        if(list.getErrorCode()!=0){
            throw new SoapException(list.getErrorMessage());
        }
        return  list.getList();
    }

	public List<String>getList(String filter, String extension) {
		List<String>list=new LinkedList<>();
		try {
			for (String string : getList())
				if(string.contains(filter)){
					if(extension != null){
						if(string.endsWith(extension)){
							list.add(string);
						}
					} else {
						list.add(string);
					}
				}
		} catch (SoapException e) {
            e.printStackTrace();
			log.error(e.getMessage(), e);
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
			log.error(e.getMessage(), e);
		}
		return list;
	}

	public List<String>getList(String[]filters, String extension) {
		return getList(new HashSet<>(Arrays.asList(filters)), extension);
	}

	public boolean removeSoapDoc(String fileName) {
		try {
            ArchiveDocRequest request = factory.createArchiveDocRequest();
            request.setUser(updUser);
            request.setFileName(fileName);
            ArchiveDocResponse response = updSoapService.archiveDoc(request);
            EdiResponse result = response.getResult();
            if(result.getErrorCode()!=0){
                throw new SoapException(result.getErrorMessage());
            }
            log.info("[{}] removed", fileName);
            return true;
		} catch (SoapException e) {
            e.printStackTrace();
			log.error(e.getMessage(), e);
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
	    return sendSoapDoc(invoiceSoapService, invoiceUser, fileName, content);
    }

    public boolean sendSoapTicket(String fileName, byte[]content) {
        return sendSoapDoc(updSoapService, updUser, fileName, content);
    }

	public boolean sendSoapDoc(ExiteWs service, EdiLogin user, String fileName, byte[]content) {
		try {
            SendDocRequest request = factory.createSendDocRequest();
            request.setUser(user);
            request.setFileName(fileName);
            request.setContent(factory.createSendDocRequestContent(content));
            SendDocResponse response = service.sendDoc(request);
            EdiResponse result = response.getResult();
            if(result.getErrorCode()!=0){
                throw new SoapException(result.getErrorMessage());
            }
            log.info("[{}] sent", fileName);
            return true;
		} catch (SoapException e) {
		    e.printStackTrace();
			log.error(e.getMessage(), e);
			return false;
		}
	}

    public byte[]getDocContent(String uuid) throws Exception {
	    try{
	        return getDocContent_(uuid);
        } catch (RestException e){
            log.error(e.getMessage(), e);
            if(e.getMessage().contains("Not authorized")){
                log.warn("Try to re-authorize");
                setupApi();
                try{
                    return getDocContent_(uuid);
                } catch (RestException e1){
                    log.error(e.getMessage());
                    return null;
                }
            }
            throw e;
        }
    }

	private byte[]getDocContent_(String uuid) throws RestException {
        Entity entity=api.getContent(authToken, uuid);
        byte[] body = Base64.getDecoder().decode(entity.getBody());
        return body;
	}

	public String getBase64TicketBody(String docUUID) throws DuplicateDocException, NoDocFoundException, RestException {
        try{
            return api.generateTicket(authToken,
                    docUUID,
                    conf.cryptex.signer.signer_name,
                    conf.cryptex.signer.signer_surName,
                    conf.cryptex.signer.signer_orgUnit,
                    conf.cryptex.signer.signer_org_inn);
        } catch (RestException e) {
            log.error("{} : doc uuid [{}]", e.getMessage(), docUUID);
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


	public boolean confirmEdoDoc(QRecord record) throws Exception {
        final String ticketName = record.getFileNameFromTicketBody().split("\\.")[0];
        log.info("[{}] created based on [{}]", ticketName, record.getFileName());
        if(sendSoapTicket(ticketName + ".xml", record.getByteArrayTicketBody()))
            return sendSoapTicket(ticketName + ".bin", record.getBase64ticketSign().getBytes());
        return false;
    }

	private String getStringSignBody(String docBody) {
		byte[] doc;
		String sign=null;
		try {
			doc = Base64.getDecoder().decode(docBody);
			sign = Base64.getEncoder().encodeToString(getSign(doc));
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}		
		return sign;
	}

    public byte[] getSign(final byte[] body) throws Exception {
		return cryptex.sign(body, conf.cryptex.alias, ESignType.DER);
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

    public void testCrypto() {
        try{
            log.info("Aliases list: ");
            for (String string : getAliases()){
                log.info("\t"+string);
                log.info("info:");
                ECertificate cert;
                try{
                    cert = getCert(string);
                } catch (Exception e){
                    log.error("ERROR while getting certificate with alias ["+string+"]: ", e);
                    continue;
                }
                log.info("valid from / to: " + cert.getValidityFrom()+" - "+cert.getValidityTo());
                log.info("toString: " + cert.toString());
                try{
                    cryptex.sign("test".getBytes(), string, ESignType.DER);
                } catch (Exception e){
                    log.error("ERROR while sign generate with certificate with alias ["+string+"]: ", e);
                    continue;
                }
                log.info("sign O.K.");
            }
        } catch (Exception e){
            log.error("Crypto-testing ERROR: ", e);
        }
    }

    public void testConnection() {
        try {
            if(this.authToken != null){
                System.out.println("https is O.K.");
            }
            getList(updSoapService, updUser);
            getList(invoiceSoapService, invoiceUser);
            System.out.println("soap is O.K.");
        } catch (SoapException e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
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
			this.api = new ExiteRestAPI();
			this.authToken = api.authorize(conf.rest.get(0).login, conf.rest.get(0).password);
			this.updUser = soapUser(conf.soap.get(0).login, conf.soap.get(0).password);
			this.invoiceUser = soapUser(conf.soap.get(1).login,conf.soap.get(1).password);

		} catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage(), e);
        }
	}

	private EdiLogin soapUser(final String login, final String rawPass){
	    final EdiLogin user = new EdiLogin();
        user.setLogin(login);
        user.setPass(DigestUtils.md5Hex(rawPass));
	    return user;
    }
}
