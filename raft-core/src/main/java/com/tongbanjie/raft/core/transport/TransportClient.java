package com.tongbanjie.raft.core.transport;

import java.lang.reflect.Method;

/***
 *
 *  transport client
 * @author banxia
 * @date 2017-12-02 16:16:23
 */
public interface TransportClient extends TransportChannel {

    /**
     * get the proxy
     *
     * @param serviceInterface
     * @param <T>
     * @return
     */
    <T> T getProxy(Class<T> serviceInterface);

    /**
     * @param method
     * @param arguments
     * @return
     */
    Object request(Method method, Object[] arguments);
}
