package org.exite.objects.rest;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by levitsky on 11.02.19
 */
@Getter
@ToString
@Builder
public class Request {
    protected String varToken;
}
