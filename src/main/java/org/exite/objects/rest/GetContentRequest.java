package org.exite.objects.rest;

import lombok.ToString;

/**
 * Created by levitsky on 11.02.19
 */
@ToString
public class GetContentRequest extends Request {
    private String identifier;

    public GetContentRequest(String varToken, String identifier) {
        super(varToken);
        this.identifier = identifier;
    }
}
