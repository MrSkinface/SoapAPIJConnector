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
public class RemoveWorker extends AbstractWorker implements Runnable {

    private static final Logger log = Logger.getLogger(RemoveWorker.class);

    private Queues in_queue;

    public RemoveWorker(QWorker worker, Queues in_queue) {

        super(worker);
        this.in_queue = in_queue;
    }

    @Override
    public void run() {
        try{
            while (execute){
                try{
                    if(super.has(in_queue)){
                        QRecord record = super.get(in_queue);
                        log.info("Got " + record.getFileName() + " from [" + in_queue.getQueueName() + "]");
                        controller.removeSoapDoc(record.getFileName(), record.getEdoStatus()==null);
                        super.clear(record);
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
