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
public class EnqueueTicketResponse extends Response {

    public EnqueueTicketResponse(String varMessage, int intCode) {
        super(varMessage, intCode);
    }
}
