package org.exite.workers;

import lombok.extern.slf4j.Slf4j;
import org.exite.beans.soap.Content;
import org.exite.workers.queues.QRecord;
import org.exite.workers.queues.QWorker;
import org.exite.workers.queues.Queues;

/**
 * Created by levitsky on 05.03.18.
 */
@Slf4j
public class InWorker extends AbstractQueueWorker {

    private Queues out_queue;

    public InWorker(QWorker worker, Queues in_queue, Queues out_queue) {
        super(worker, in_queue);
        this.out_queue = out_queue;
    }

    @Override
    protected void executeQueue() throws Exception {
        QRecord record = super.get(in_queue);
        try{
            final Content content = controller.getContent(record.getFileName());
            record.setBody(content.getBody());
            record.setSign(content.getSign());
            super.put(out_queue, record);
        } catch (Exception e){
            log.error(e.getMessage(), e);
            super.clear(record);
        }
    }
}
