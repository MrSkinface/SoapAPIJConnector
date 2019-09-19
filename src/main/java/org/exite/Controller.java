package org.exite;

import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.exite.beans.soap.Content;
import org.exite.beans.tickets.*;
import org.exite.crypt.CryptEx;
import org.exite.crypt.ECertificate;
import org.exite.crypt.ESignType;
import org.exite.service.SoapException;
import org.exite.beans.config.Config;
import org.exite.service.ExiteSoap;
import org.exite.service.ISoapService;
import org.exite.service.tickets.TicketGenerator;
import org.exite.utils.ZipContainer;
import org.exite.workers.queues.QRecord;

@Slf4j
public class Controller {
	
	private Config conf;
    private CryptEx cryptex;

    private ISoapService invoiceSoap;
    private ISoapService updSoap;

	
	public Controller(final Config conf) throws Exception {
		this.conf = conf;
        cryptex = new CryptEx(conf.getCertStorePass());
        this.updSoap = new ExiteSoap(conf.soap.get(0).login, conf.soap.get(0).password);
        this.invoiceSoap = new ExiteSoap(conf.soap.get(1).login,conf.soap.get(1).password);
	}

	public List<String>uzdList(String[]filters, String extension) throws Exception {
		return updSoap.list(filters, extension);
	}

    public void removeUzd(String fileName, boolean removewithSign) throws Exception {
        updSoap.remove(fileName, removewithSign);
    }

    public boolean sendForConvertation(String fileName, byte[]content) throws Exception {
	    return invoiceSoap.send(fileName, content);
    }

    public boolean sendSoapTicket(String fileName, byte[]content) throws Exception {
        return updSoap.send(fileName, content);
    }

    public Content getContent(String fileName) throws Exception {
        final byte[]body = updSoap.body(fileName);
        if(fileName.endsWith(".zip")){
            final ZipContainer zip = new ZipContainer();
            zip.unzip(body);
            return Content.builder()
                    .body(zip.getXml())
                    .sign(zip.getBin())
                    .build();
        } else {
            return Content.builder()
                    .body(body)
                    .sign(updSoap.body(fileName.replace(".xml", ".bin")))
                    .build();
        }
    }

    public QRecord prepareTicket(QRecord record) throws Exception {
        final TicketGenerator generator;
        if(record.isUPD()){
            generator = new IzvpolForUpd(record.getFileName());
        } else {
            generator = new IzvpolForPdotpr();
        }

        final Signer signer = Signer.builder()
                .firstName(conf.cryptex.signer.signer_name)
                .lastName(conf.cryptex.signer.signer_surName)
                .position(conf.cryptex.signer.signer_orgUnit)
                .position(conf.cryptex.signer.signer_org_inn)
                .build();

        final TicketGeneratorData ticketData = new TicketGeneratorData.Builder()
                .setBaseTicketBody(record.getBody())
                .setBaseTicketSign(new String(record.getSign()))
                .setSigner(signer)
                .build();

        final byte[] ticketBody = generator.generate(ticketData);
        final byte[] ticketSign = sign(ticketBody);
        final String ticketName = ((IzvpolTicket) generator).getFileName();

        record.setTicketName(ticketName);
        record.setTicketBody(ticketBody);
        record.setTicketSign(ticketSign);
        return record;
    }

	public boolean confirmEdoDoc(QRecord record) throws Exception {
        log.info("[{}] created based on [{}]", record.getTicketName(), record.getFileName());
        if(sendSoapTicket(record.getTicketName() + ".xml", record.getTicketBody()))
            return sendSoapTicket(record.getTicketName() + ".bin", Base64.getEncoder().encode(record.getTicketSign()));
        return false;
    }

    public byte[] sign(final byte[] body) throws Exception {
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
            updSoap.list(new String[]{}, null);
            invoiceSoap.list(new String[]{}, null);
            System.out.println("soap is O.K.");
        } catch (SoapException e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
        }
    }

    public void checkCryptexAlive(){
        try{
            this.sign("test".getBytes());
        } catch (Exception e){
            log.error("[FATAL ERROR] Cryptex fails :", e);
            System.exit(500);
        }
    }
}
