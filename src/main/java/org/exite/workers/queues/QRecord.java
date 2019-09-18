package org.exite.workers.queues;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.util.Base64;

/**
 * Created by levitsky on 05.03.18.
 */
public class QRecord {

    private String fileName;

    private byte[] body;

    /*
    * base64 ticket and sign
    * */
    private String base64ticketBody;

    private String base64ticketSign;

    private boolean sendXMLBack = false;

    public QRecord(String fileName) {
        this.fileName = fileName;
    }

    public String getBase64ticketBody() {
        return base64ticketBody;
    }

    public byte[] getByteArrayTicketBody(){
        return Base64.getDecoder().decode(this.getBase64ticketBody());
    }

    public void setBase64ticketBody(String base64ticketBody) {
        this.base64ticketBody = base64ticketBody;
    }

    public String getBase64ticketSign() {
        return base64ticketSign;
    }

    public void setBase64ticketSign(String base64ticketSign) {
        this.base64ticketSign = base64ticketSign;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public boolean needBody() {
        return getFileName().startsWith("ON_SCHFDOPPR") || getFileName().startsWith("ON_KORSCHFDOPPR") ||
                getFileName().startsWith("ON_NSCHFDOPPR") || getFileName().startsWith("ON_NKORSCHFDOPPR");
    }

    public boolean isSendXMLBack() {
        return sendXMLBack;
    }

    public void setSendXMLBack(boolean sendXMLBack) {
        this.sendXMLBack = sendXMLBack;
    }

    public String getSoapSendName() {
        String prefix = null;
        if(getFileName().startsWith("ON_SCHFDOPPR") || getFileName().startsWith("ON_NSCHFDOPPR")){
            prefix = "upd_";
        } else if(getFileName().startsWith("ON_KORSCHFDOPPR") || getFileName().startsWith("ON_NKORSCHFDOPPR")){
            prefix = "ukd_";
        }
        return prefix + getUUID() + ".xml";
    }

    public String getUUID() {
        return getFileName().split("_")[5].split("\\.")[0];
    }

    public String getFileNameFromTicketBody() throws Exception{
        ByteArrayInputStream bais = new ByteArrayInputStream(getByteArrayTicketBody());
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        org.w3c.dom.Document doc = builder.parse(bais);
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = xpath.compile("/Файл/@ИдФайл");
        return (String) expr.evaluate(doc, XPathConstants.STRING);
    }

    @Override
    public String toString() {
        return "QRecord{" +
                "fileName='" + fileName + '\'' +
                '}';
    }
}
