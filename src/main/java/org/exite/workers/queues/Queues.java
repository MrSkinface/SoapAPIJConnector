package org.exite.workers.queues;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by levitsky on 07.03.18.
 */
public enum Queues {

    INBOUND_UPD("inbound_upd"),

    INBOUND_DOCS("inbound_docs"),

    OUTBOUND_UPD("outbound_upd"),

    OUTBOUND_TICKETS("outbound_tickets"),

    TO_SIGN_UPD("to_sign_upd"),

    TO_SIGN_DOCS("to_sign_docs"),

    TO_REMOVE("to_remove");

    private String queueName;

    public String getQueueName() {
        return queueName;
    }

    Queues(String queueName) {
        this.queueName = queueName;
    }

    public static List<String> all(){
        List<String> qNames = new LinkedList<>();
        for(Queues q : Queues.values()){
            qNames.add(q.getQueueName());
        }
        return qNames;
    }
}
