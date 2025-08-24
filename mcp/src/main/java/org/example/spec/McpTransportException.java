package org.example.spec;

import java.io.Serial;

public class McpTransportException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public McpTransportException(String message) {
        super(message);
    }

    public McpTransportException(String message, Throwable cause) {
        super(message, cause);
    }

    public McpTransportException(Throwable cause) {
        super(cause);
    }

    public McpTransportException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {

        super(message, cause, enableSuppression, writableStackTrace);
    }
}
