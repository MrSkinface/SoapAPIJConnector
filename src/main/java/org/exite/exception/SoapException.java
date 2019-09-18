package org.exite.exception;

/**
 * Created by levitsky on 04.02.19
 */
public class SoapException extends Exception {

    private static final long serialVersionUID = 1L;

    public SoapException(String message) {
        super(message);
    }

    public SoapException(String message, Throwable cause) {
        super(message, cause);
    }
}
