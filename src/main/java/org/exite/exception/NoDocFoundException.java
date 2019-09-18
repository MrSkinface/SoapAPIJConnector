package org.exite.exception;

/**
 * Created by levitsky on 07.03.18.
 */
public class NoDocFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    public NoDocFoundException(String message) {
        super(message);
    }

    public NoDocFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
