package org.exite.objects.rest;

import lombok.ToString;

/**
 * Created by levitsky on 11.02.19
 */
@ToString
public class EnqueueTicketRequest extends Request {
    private String identifier;
    private String xml;
    private String sign;

    public EnqueueTicketRequest(String varToken, String identifier, String xml, String sign) {
        super(varToken);
        this.identifier = identifier;
        this.xml = xml;
        this.sign = sign;
    }
}
