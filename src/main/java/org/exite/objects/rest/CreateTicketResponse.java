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
public class CreateTicketResponse extends Response {
    private String content;

    public CreateTicketResponse(String varMessage, int intCode, String content) {
        super(varMessage, intCode);
        this.content = content;
    }
}
