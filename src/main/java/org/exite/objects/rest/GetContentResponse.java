package org.exite.objects.rest;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * Created by levitsky on 11.02.19
 */
@Getter
@NoArgsConstructor
@ToString
public class GetContentResponse extends Response {
    private String body;
    private List<Sign> sign;

    public GetContentResponse(String varMessage, int intCode, String body, List<Sign> sign) {
        super(varMessage, intCode);
        this.body = body;
        this.sign = sign;
    }
}
