package org.exite.objects.rest;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Created by levitsky on 11.02.19
 */
@Getter
@NoArgsConstructor
@ToString
public class AuthorizeResponse extends Response {
    private String varToken;

    public AuthorizeResponse(String varMessage, int intCode, String varToken) {
        super(varMessage, intCode);
        this.varToken = varToken;
    }
}
