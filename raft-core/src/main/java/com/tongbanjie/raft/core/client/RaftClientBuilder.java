package com.tongbanjie.raft.core.client;

import com.tongbanjie.raft.core.transport.TransportClient;
import com.tongbanjie.raft.core.transport.builder.NettyClientBuilder;
import com.tongbanjie.raft.core.transport.netty.serialization.support.Hessian2Serialization;
import com.tongbanjie.raft.core.transport.proxy.support.JdkTransportClientProxy;

/***
 *
 * @author banxia
 * @date 2017-11-30 15:15:16
 */
public class RaftClientBuilder<T> {

    private String host;

    private int port;

    private long timeout;

    private Class<T> serviceInterface;

    private TransportClient transportClient;


    public RaftClientBuilder<T> host(String host) {
        this.host = host;
        return this;
    }

    public RaftClientBuilder<T> port(int port) {
        this.port = port;
        return this;
    }

    public RaftClientBuilder<T> timeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    public RaftClientBuilder<T> serviceInterface(Class<T> serviceInterface) {
        this.serviceInterface = serviceInterface;
        return this;
    }

    public TransportClient getTransportClient() {
        return transportClient;
    }

    public T builder() {

        NettyClientBuilder<T> nettyClientBuilder = new NettyClientBuilder<T>();
        T builder = nettyClientBuilder.port(this.port)
                .host(this.host)
                .serialization(new Hessian2Serialization())
                .serviceInterface(this.serviceInterface)
                .requestTimeout(timeout)
                .transportClientProxy(new JdkTransportClientProxy()).builder();

        this.transportClient = nettyClientBuilder.getTransportClient();
        return builder;

    }


}
