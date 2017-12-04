package com.tongbanjie.raft.core.transport.proxy;

import com.tongbanjie.raft.core.transport.TransportChannel;
import com.tongbanjie.raft.core.transport.TransportClient;

/***
 *
 * @author banxia
 * @date 2017-12-02 22:22:55
 */
public interface TransportClientProxy {


    <T> T getProxyClient(Class<T> serviceInterface, TransportClient channel);
}
