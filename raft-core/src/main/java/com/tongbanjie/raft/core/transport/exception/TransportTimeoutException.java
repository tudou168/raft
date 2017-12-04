package com.tongbanjie.raft.core.transport.exception;

/***
 *
 * @author banxia
 * @date 2017-12-02 17:17:21
 */
public class TransportTimeoutException extends RuntimeException {

    public TransportTimeoutException() {
        super("transport timeout exception");
    }

    public TransportTimeoutException(String message) {
        super(message);
    }

    public TransportTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransportTimeoutException(Throwable cause) {
        super(cause);
    }
}
