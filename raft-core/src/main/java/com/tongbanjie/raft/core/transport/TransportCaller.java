package com.tongbanjie.raft.core.transport;

import java.lang.reflect.Method;

/***
 *
 * @author banxia
 * @date 2017-12-03 23:23:17
 */
public interface TransportCaller extends TransportChannel {

    ResponseFuture request(Request request);
}
