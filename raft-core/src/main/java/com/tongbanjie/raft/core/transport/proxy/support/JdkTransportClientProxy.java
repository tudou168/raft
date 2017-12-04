package com.tongbanjie.raft.core.transport.proxy.support;

import com.tongbanjie.raft.core.transport.TransportClient;
import com.tongbanjie.raft.core.transport.proxy.TransportClientProxy;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/***
 *
 * @author banxia
 * @date 2017-12-02 22:22:58
 */
public class JdkTransportClientProxy implements TransportClientProxy {
    @Override
    public <T> T getProxyClient(Class<T> serviceInterface, final TransportClient channel) {

        Object proxy = Proxy.newProxyInstance(channel.getClass().getClassLoader(), new Class[]{serviceInterface}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                String methodName = method.getName();
                if (StringUtils.equals("equals", methodName)) {
                    return proxyEquals(proxy, args[0]);
                } else if (StringUtils.equals("toString", methodName)) {
                    return proxyToString(proxy);
                } else if (StringUtils.equals("hashCode", methodName)) {
                    return proxyHashCode(proxy);
                }

                return channel.request(method, args);
            }
        });
        return (T) proxy;
    }

    private Object proxyHashCode(Object proxy) {
        return System.identityHashCode(proxy);
    }

    private Object proxyToString(Object proxy) {
        return proxy.getClass().getName() + "@proxy@" + Integer.toHexString(proxy.hashCode());
    }

    private Object proxyEquals(Object proxy, Object other) {
        return proxy == other;
    }


}
