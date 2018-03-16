package org.exite.workers;

import org.apache.log4j.Logger;
import org.exite.Controller;
import org.exite.obj.Config;
import org.exite.workers.queues.QRecord;
import org.exite.workers.queues.QWorker;
import org.exite.workers.queues.Queues;

/**
 * Created by levitsky on 05.03.18.
 */
public class SignWorker extends AbstractWorker implements Runnable {

    private static final Logger log = Logger.getLogger(SignWorker.class);

    private Queues in_queue;
    private Queues out_queue_upd;
    private Queues out_queue_tickets;

    public SignWorker(QWorker worker, Queues in_queue, Queues out_queue_upd, Queues out_queue_tickets) {

        super(worker);
        this.in_queue = in_queue;
        this.out_queue_upd = out_queue_upd;
        this.out_queue_tickets = out_queue_tickets;
    }

    @Override
    public void run() {
        try{
            while (execute) {
                try{
                    if(super.has(in_queue)){

                        QRecord record = super.get(in_queue);
                        log.info("Got " + record.getFileName() + " from [" + in_queue.getQueueName() + "]");
                        record = getSignedTicket(record);
                        if(out_queue_upd != null){
                            record.setSendXMLBack(true);
                            super.put(out_queue_upd, record);
                            log.info("Put " + record.getFileName() + " to [" + out_queue_upd.getQueueName() + "]");
                        } else {
                            super.put(out_queue_tickets, record);
                            log.info("Put " + record.getFileName() + " to [" + out_queue_tickets.getQueueName() + "]");
                        }

                    } else {
                        super.waitFor(3);
                    }
                }catch (Exception e){
                    log.error(e);
                }
            }

        }catch (Exception e){
            log.error(e);
        }
    }

}
