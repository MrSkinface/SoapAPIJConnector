package org.exite.workers;

import org.exite.workers.queues.QWorker;
import org.exite.workers.queues.Queues;

/**
 * Created by levitsky on 11.02.19
 */
public abstract class AbstractQueueWorker extends AbstractWorker {

    protected Queues in_queue;

    public AbstractQueueWorker(QWorker worker, Queues in_queue) {
        super(worker);
        this.in_queue = in_queue;
    }

    protected abstract void executeQueue() throws Exception;

    @Override
    protected void execute() throws Exception {
        if(super.has(in_queue)){
            executeQueue();
        } else {
            waitFor();
        }
    }
}
