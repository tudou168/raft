package com.tongbanjie.raft.core.transport;

/***
 *
 * @author banxia
 * @date 2017-12-04 11:11:14
 */
public interface FutureListener {


    void complete(Future future);
}
