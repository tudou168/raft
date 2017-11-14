package com.tongbanjie.raft.core.exception;

/***
 *  raft exception
 *  //TODO
 * @author banxia
 * @date 2017-11-14 18:18:01
 */
public class RaftException extends RuntimeException {

    public RaftException() {
    }

    public RaftException(String message) {
        super(message);
    }

    public RaftException(String message, Throwable cause) {
        super(message, cause);
    }

    public RaftException(Throwable cause) {
        super(cause);
    }
}
