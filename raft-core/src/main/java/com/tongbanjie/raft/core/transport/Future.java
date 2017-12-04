package com.tongbanjie.raft.core.transport;

/***
 *
 * @author banxia
 * @date 2017-12-04 11:11:08
 */
public interface Future {

    // cancel the task
    boolean cancel();

    boolean isCanceled();

    boolean isDone();

    boolean isSuccess();

    Object getValue();

    Exception getException();

    void addListener(FutureListener listener);
}
