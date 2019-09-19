package org.exite.workers.queues;

/**
 * Created by levitsky on 05.03.18.
 */
public class QRecord {

    private String fileName;

    private byte[] body;
    private byte[] sign;

    private byte[] ticketBody;
    private byte[] ticketSign;
    private String ticketName;

    private boolean sendXMLBack = false;

    public QRecord(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public boolean isUPD() {
        return getFileName().startsWith("ON_SCHFDOPPR") || getFileName().startsWith("ON_KORSCHFDOPPR") ||
                getFileName().startsWith("ON_NSCHFDOPPR") || getFileName().startsWith("ON_NKORSCHFDOPPR");
    }

    public byte[] getSign() {
        return sign;
    }

    public void setSign(byte[] sign) {
        this.sign = sign;
    }

    public byte[] getTicketBody() {
        return ticketBody;
    }

    public void setTicketBody(byte[] ticketBody) {
        this.ticketBody = ticketBody;
    }

    public byte[] getTicketSign() {
        return ticketSign;
    }

    public void setTicketSign(byte[] ticketSign) {
        this.ticketSign = ticketSign;
    }

    public String getTicketName() {
        return ticketName;
    }

    public void setTicketName(String ticketName) {
        this.ticketName = ticketName;
    }

    public String getFileName() {
        return fileName;
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

    @Override
    public String toString() {
        return "QRecord {" +
                "fileName='" + fileName + '\'' +
                '}';
    }
}
