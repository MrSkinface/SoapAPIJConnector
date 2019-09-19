package org.exite.workers;

import lombok.extern.slf4j.Slf4j;
import org.exite.workers.queues.QRecord;
import org.exite.workers.queues.QWorker;
import org.exite.workers.queues.Queues;

/**
 * Created by levitsky on 05.03.18.
 */
@Slf4j
public class SignWorker extends AbstractQueueWorker {

    private Queues out_queue_upd;
    private Queues out_queue_tickets;

    public SignWorker(QWorker worker, Queues in_queue, Queues out_queue_upd, Queues out_queue_tickets) {

        super(worker, in_queue);
        this.out_queue_upd = out_queue_upd;
        this.out_queue_tickets = out_queue_tickets;
    }

    @Override
    protected void executeQueue() throws Exception {
        QRecord record = super.get(in_queue);
        try{
            record = controller.prepareTicket(record);
            if(out_queue_upd != null){
                record.setSendXMLBack(true);
                super.put(out_queue_upd, record);
            } else {
                super.put(out_queue_tickets, record);
            }
        } catch (Exception e){
            log.error(e.getMessage(), e);
            super.clear(record);
        }
    }
}
