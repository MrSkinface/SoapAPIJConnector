package org.exite.workers.queues;

import org.exite.obj.SystemStatus;

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

    private SystemStatus edoStatus;

    public QRecord(String fileName) {
        this.fileName = fileName;
    }

    public String getBase64ticketBody() {
        return base64ticketBody;
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
        return getFileName().startsWith("ON_SCHFDOPPR") || getFileName().startsWith("ON_KORSCHFDOPPR") || getFileName().startsWith("EDOSTATUS");
    }

    public SystemStatus getEdoStatus() {
        return edoStatus;
    }

    public String getDocUUIDFromStatus(){
        try{
            return getEdoStatus().DOCID;
        } catch (NullPointerException e){
            return null;
        }
    }

    public String getRejectCommentFromStatus(){
        try{
            if(getEdoStatus() == null){
                return null;
            }
            if(getStatusCodeFromStatus().equalsIgnoreCase("ERROR")){
                return getEdoStatus().COMMENT;
            }
            return null;
        } catch (NullPointerException e){
            return null;
        }
    }

    public String getStatusCodeFromStatus(){
        try{
            return getEdoStatus().STATUSCODE;
        } catch (NullPointerException e){
            return null;
        }
    }

    public void setEdoStatus(SystemStatus edoStatus) {
        this.edoStatus = edoStatus;
    }

    public boolean isSendXMLBack() {
        return sendXMLBack;
    }

    public void setSendXMLBack(boolean sendXMLBack) {
        this.sendXMLBack = sendXMLBack;
    }

    public String getSoapSendName() {
        String prefix = null;
        if(getFileName().startsWith("ON_SCHFDOPPR")){
            prefix = "upd_";
        } else if(getFileName().startsWith("ON_KORSCHFDOPPR")){
            prefix = "ukd_";
        }
        return prefix + getUUID() + ".xml";
    }

    public String getUUID() {
        return edoStatus == null ? getFileName().split("_")[5].split("\\.")[0] : getDocUUIDFromStatus();
    }

    @Override
    public String toString() {
        return "QRecord{" +
                "fileName='" + fileName + '\'' +
                '}';
    }
}
