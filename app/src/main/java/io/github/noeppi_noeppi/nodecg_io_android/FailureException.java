package io.github.noeppi_noeppi.nodecg_io_android;

public class FailureException extends Exception {

    public FailureException() {
        super();
    }

    public FailureException(String message) {
        super(message);
    }

    public FailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailureException(Throwable cause) {
        super(cause);
    }

    protected FailureException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
