package org.exite.workers;

import lombok.extern.slf4j.Slf4j;
import org.exite.workers.queues.QRecord;
import org.exite.workers.queues.QWorker;
import org.exite.workers.queues.Queues;

import java.util.List;

/**
 * Created by levitsky on 05.03.18.
 */
@Slf4j
public class InQueueCreator extends AbstractWorker {

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
    protected void beforeExecute() throws Exception {
        checkCryptexAlive();
    }

    @Override
    protected void afterExecute() throws Exception {
        sleep(sleepTime);
    }

    @Override
    protected void execute() throws Exception {
        log.info("{} for [{}] start", this.getClass().getSimpleName(), out_queue.getQueueName());
        final List<String> files = controller.getList(types, extension);
        for (String fileName : files) {
            QRecord record = new QRecord(fileName);
            super.put(out_queue, record);
        }
        log.info("end. Waiting {} sec ...", sleepTime);
    }
}
