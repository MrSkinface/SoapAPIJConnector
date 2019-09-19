package org.exite.workers;

import lombok.extern.slf4j.Slf4j;
import org.exite.workers.queues.QRecord;
import org.exite.workers.queues.QWorker;
import org.exite.workers.queues.Queues;

/**
 * Created by levitsky on 05.03.18.
 */
@Slf4j
public class OutWorker extends AbstractQueueWorker {

    private Queues out_queue;

    public OutWorker(QWorker worker, Queues in_queue, Queues out_queue) {
        super(worker, in_queue);
        this.out_queue = out_queue;
    }

    @Override
    protected void executeQueue() throws Exception {
        QRecord record = super.get(in_queue);
        try{
            if(record.isSendXMLBack()){
                if(controller.sendForConvertation(record.getSoapSendName(), record.getBody()))
                    if(controller.confirmEdoDoc(record))
                        super.put(out_queue, record);
            } else {
                if(controller.confirmEdoDoc(record))
                    super.put(out_queue, record);
            }
        } catch (Exception e){
            log.error(e.getMessage(), e);
            super.clear(record);
        }
    }
}
