package com.tongbanjie.raft.core.exception;

/***
 *
 * @author banxia
 * @date 2017-12-02 17:17:22
 */
public class TransportMethodNotFoundException extends RuntimeException {

    public TransportMethodNotFoundException() {
        super("transport  method not found  exception");
    }

    public TransportMethodNotFoundException(String message) {
        super(message);
    }

    public TransportMethodNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransportMethodNotFoundException(Throwable cause) {
        super(cause);
    }
}
