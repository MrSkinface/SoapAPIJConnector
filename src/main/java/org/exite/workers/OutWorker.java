package org.exite.workers;

import org.apache.log4j.Logger;
import org.exite.Controller;
import org.exite.exception.DuplicateDocException;
import org.exite.exception.NoDocFoundException;
import org.exite.obj.Config;
import org.exite.workers.queues.QRecord;
import org.exite.workers.queues.QWorker;
import org.exite.workers.queues.Queues;

/**
 * Created by levitsky on 05.03.18.
 */
public class OutWorker extends AbstractWorker implements Runnable {

    private static final Logger log = Logger.getLogger(OutWorker.class);

    private Queues in_queue;
    private Queues out_queue;

    public OutWorker(QWorker worker, Queues in_queue, Queues out_queue) {

        super(worker);
        this.in_queue = in_queue;
        this.out_queue = out_queue;
    }

    @Override
    public void run() {
        try{
            while (execute){
                if(super.has(in_queue)){
                    QRecord record = null;
                    try{
                        record = super.get(in_queue);
                        log.info("Got " + record.getFileName() + " from [" + in_queue.getQueueName() + "]");
                        if(record.isSendXMLBack()){
                            controller.sendSoapDoc(record.getSoapSendName(), record.getBody());
                        }
                        controller.confirmEdoDoc(record);
                        super.put(out_queue, record);
                        log.info("Put " + record.getFileName() + " to [" + out_queue.getQueueName() + "]");
                    } catch (DuplicateDocException | NoDocFoundException e){
                        super.put(out_queue, record);
                        log.info("Put " + record.getFileName() + " to [" + out_queue.getQueueName() + "]");
                    } catch (Exception e){
                        log.error(e);
                    }
                } else {
                    super.waitFor(3);
                }
            }

        }catch (Exception e){
            log.error(e);
        }
    }

}
