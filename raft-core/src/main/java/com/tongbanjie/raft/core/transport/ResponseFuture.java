package com.tongbanjie.raft.core.transport;

/***
 *
 * @author banxia
 * @date 2017-12-04 11:11:18
 */
public interface ResponseFuture extends Future {

    void onSuccess(Response response);

    void onFailure(Response response);

    long getCreateTime();

    long getRequestId();

    long getProcessTime();

}
