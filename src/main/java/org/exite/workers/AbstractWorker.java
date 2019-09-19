package org.exite.workers;

import lombok.extern.slf4j.Slf4j;
import org.exite.Controller;
import org.exite.beans.config.Config;
import org.exite.workers.queues.QRecord;
import org.exite.workers.queues.QWorker;
import org.exite.workers.queues.Queues;

/**
 * Created by levitsky on 06.03.18.
 */
@Slf4j
public abstract class AbstractWorker extends Daemon {

    protected Config conf;
    protected Controller controller;

    private QWorker worker;

    public AbstractWorker(QWorker worker) {
        this.conf = worker.getConf();
        this.controller = worker.getController();
        this.worker = worker;
        execute = conf.execute;
        sleepTime = conf.sleepTime;
    }

    @Override
    protected void onInit() throws Exception {

    }

    @Override
    protected void beforeExecute() throws Exception {

    }

    @Override
    protected void afterExecute() throws Exception {

    }

    protected boolean has(Queues queue){
        return !worker.getQueue(queue).isEmpty();
    }

    protected QRecord get(Queues queue){
        final QRecord record = worker.getRecord(queue);
        log.info("Got {} from [{}]", record.getFileName(), queue.getQueueName());
        return record;
    }

    protected void clear(QRecord record){
        worker.remove(record);
    }

    protected boolean put(Queues queue, QRecord record){
        final boolean res = worker.putRecord(queue, record);
        if(res)
            log.info("Put {} to [{}]", record.getFileName(), queue.getQueueName());
        return res;
    }

    protected void waitFor(){
        waitFor(1);
    }

    private final void waitFor(long seconds){
        try{
            Thread.currentThread().sleep(seconds * 1000);
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

}
