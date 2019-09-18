package org.exite.workers;

import lombok.extern.slf4j.Slf4j;
import org.exite.workers.queues.QRecord;
import org.exite.workers.queues.QWorker;
import org.exite.workers.queues.Queues;

/**
 * Created by levitsky on 05.03.18.
 */
@Slf4j
public class RemoveWorker extends AbstractQueueWorker {

    public RemoveWorker(QWorker worker, Queues in_queue) {
        super(worker, in_queue);
    }

    @Override
    protected void executeQueue() throws Exception {
        QRecord record = super.get(in_queue);
        controller.removeSoapDoc(record.getFileName(), true);
        super.clear(record);
    }
}
