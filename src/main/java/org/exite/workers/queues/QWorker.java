package org.exite.workers.queues;

import org.exite.Controller;
import org.exite.objects.config.Config;

import java.util.*;

/**
 * Created by levitsky on 05.03.18.
 */
public class QWorker {

    private Map<String, Queue> queues = new HashMap<>();

    private Set<String> all_docs = new HashSet<>();

    private final Config conf;

    private final Controller controller;

    public QWorker(Config conf, Controller controller) {
        this.conf = conf;
        this.controller = controller;
        initQueues();
    }

    public Config getConf() {
        return conf;
    }

    public Controller getController() {
        return controller;
    }

    private void initQueues(){
        for (String qName : Queues.all()){
            queues.put(qName, new LinkedList<>());
        }
    }

    public Queue getQueue(Queues queue){
        return queues.get(queue.getQueueName());
    }

    public boolean putRecord(Queues queue, QRecord record){
        if (!contains(record)){
            getQueue(queue).offer(record);
            all_docs.add(record.getFileName());
            return true;
        }
        return false;
    }

    public QRecord getRecord(Queues queue){
        QRecord record = (QRecord)getQueue(queue).poll();
        all_docs.remove(record.getFileName());
        return record;
    }

    public void remove(QRecord record){
        all_docs.remove(record.getFileName());
    }

    private boolean contains(QRecord record){
        return all_docs.contains(record.getFileName());
    }
}
