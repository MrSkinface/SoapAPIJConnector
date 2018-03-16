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
 * Created by levitsky on 05.03.18.
 */
public class InWorker extends AbstractWorker implements Runnable {

    private static final Logger log = Logger.getLogger(InWorker.class);

    private Queues in_queue;
    private Queues out_queue;

    public InWorker(QWorker worker, Queues in_queue, Queues out_queue) {

        super(worker);
        this.in_queue = in_queue;
        this.out_queue = out_queue;
    }

    @Override
    public void run() {
        try{
            while (execute) {
                try{
                    if(super.has(in_queue)){
                        QRecord record = super.get(in_queue);
                        log.info("Got " + record.getFileName() + " from [" + in_queue.getQueueName() + "]");
                        if(record.needBody()){
                            record = getBody(record);
                        }
                        super.put(out_queue, record);
                        log.info("Put " + record.getFileName() + " to [" + out_queue.getQueueName() + "]");
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
