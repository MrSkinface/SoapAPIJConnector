package org.exite.objects.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Created by levitsky on 11.02.19
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Response {
    protected String varMessage;
    protected int intCode;
}
