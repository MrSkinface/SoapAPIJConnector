package org.exite.exception;

/**
 * Created by levitsky on 05.03.18.
 */
public class DuplicateDocException extends Exception {

    private static final long serialVersionUID = 1L;

    public DuplicateDocException(String message) {
        super(message);
    }

    public DuplicateDocException(String message, Throwable cause) {
        super(message, cause);
    }
}
