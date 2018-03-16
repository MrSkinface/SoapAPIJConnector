package org.exite.workers;

import org.apache.log4j.Logger;
import org.exite.Controller;
import org.exite.obj.Config;
import org.exite.workers.queues.QRecord;
import org.exite.workers.queues.QWorker;
import org.exite.workers.queues.Queues;

import java.util.List;

/**
 * Created by levitsky on 05.03.18.
 */
public class InQueueCreator extends AbstractWorker implements Runnable {

    private static final Logger log = Logger.getLogger(InQueueCreator.class);

    private Queues out_queue;
    private String[] types;

    private String extension;

    public InQueueCreator(QWorker worker, Queues out_queue, String[] types, String extension) {

        super(worker);
        this.types = types;
        this.out_queue = out_queue;
        this.extension = extension;
    }

    @Override
    public void run() {
        try{
            while (execute){
                try{
                    super.checkCryptexAlive();
                    log.info(this.getClass().getSimpleName() + " for [" + out_queue.getQueueName() + "] start");

                    final List<String> files = controller.getList(types, extension);
                    for (String fileName : files) {
                        QRecord record = new QRecord(fileName);
                        if(super.put(out_queue, record)){
                            log.info("Put " + record.getFileName() + " to [" + out_queue.getQueueName() + "]");
                        }
                    }

                    log.info("end. Waiting " + sleepTime + " sec ...");
                    Thread.sleep(sleepTime * 1000);

                }catch (Exception e){
                    e.printStackTrace();
                    log.error(e);
                }
            }
        }catch (Exception e){
            log.error(e);
        }
    }
}
