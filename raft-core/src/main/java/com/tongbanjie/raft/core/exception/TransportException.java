package com.tongbanjie.raft.core.exception;

/***
 *
 * @author banxia
 * @date 2017-12-02 17:17:21
 */
public class TransportException extends RuntimeException {

    public TransportException() {
        super("transport exception");
    }

    public TransportException(String message) {
        super(message);
    }

    public TransportException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransportException(Throwable cause) {
        super(cause);
    }
}
