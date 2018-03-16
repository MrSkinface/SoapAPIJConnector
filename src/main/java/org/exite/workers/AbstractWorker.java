package org.exite.workers;

import org.apache.log4j.Logger;
import org.exite.Controller;
import org.exite.obj.Config;
import org.exite.obj.SystemStatus;
import org.exite.utils.Parser;
import org.exite.workers.queues.QRecord;
import org.exite.workers.queues.QWorker;
import org.exite.workers.queues.Queues;

/**
 * Created by levitsky on 06.03.18.
 */
public abstract class AbstractWorker {

    private static final Logger log = Logger.getLogger(AbstractWorker.class);

    protected Config conf;
    protected Controller controller;

    protected boolean execute = false;
    protected long sleepTime;

    public AbstractWorker(QWorker worker) {
        this.conf = worker.getConf();
        this.controller = worker.getController();
        this.worker = worker;
        execute = Boolean.valueOf(conf.execute);
        sleepTime = Long.valueOf(conf.sleepTime);
    }

    private QWorker worker;

    protected boolean has(Queues queue){
        return !worker.getQueue(queue).isEmpty();
    }

    protected QRecord get(Queues queue){
        return worker.getRecord(queue);
    }

    protected void clear(QRecord record){
        worker.remove(record);
    }

    protected boolean put(Queues queue, QRecord record){
        return worker.putRecord(queue, record);
    }

    protected void waitFor(long seconds){
        try{
            Thread.currentThread().sleep(seconds * 1000);
        }catch (Exception e){
            log.error(e);
        }
    }

    protected QRecord getBody(QRecord record){
        QRecord rec = record;
        byte[] body;
        try{

            if(record.getFileName().startsWith("EDOSTATUS")){
                body = controller.getDoc(record.getFileName());
                record.setEdoStatus((SystemStatus) Parser.fromXml(body, SystemStatus.class));
            } else {
                body = controller.getDocContent(record.getUUID());
            }

            record.setBody(body);
        }catch (Exception e){
            log.error(e);
        }
        return rec;
    }

    protected QRecord getSignedTicket(QRecord record){
        QRecord r = record;
        try{

            final String base64ticketBody = controller.getBase64TicketBody(record.getUUID(), record.getRejectCommentFromStatus());
            final String base64ticketSign = controller.getBase64TicketSign(base64ticketBody);
            record.setBase64ticketBody(base64ticketBody);
            record.setBase64ticketSign(base64ticketSign);

        }catch (Exception e){
            log.error(e);
        }
        return r;
    }

    protected void checkCryptexAlive(){
        try{
            controller.getSign("test".getBytes());
        } catch (Exception e){
            log.error("[FATAL ERROR] Cryptex fails :", e);
            this.execute = false;
            System.exit(500);
        }
    }

}
