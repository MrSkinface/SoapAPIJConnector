package org.exite.service.tickets;

import org.exite.beans.tickets.TicketGeneratorData;

import java.text.SimpleDateFormat;
import java.util.UUID;

/**
 * Created by levitskym on 18.09.19
 */
public interface TicketGenerator {

    byte[] generate(final TicketGeneratorData data) throws Exception;

    default String getFileName(String doc_type, String recipient, String sender) {
        final StringBuilder sb = new StringBuilder();
        sb.append(doc_type);
        sb.append("_");
        sb.append(recipient);
        sb.append("_");
        sb.append(sender);
        sb.append("_");
        sb.append(new SimpleDateFormat("yyyyMMdd")
                .format(System.currentTimeMillis()));
        sb.append("_");
        sb.append(UUID.randomUUID().toString());
        return sb.toString();
    }
}
