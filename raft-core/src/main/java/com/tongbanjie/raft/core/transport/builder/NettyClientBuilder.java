package com.tongbanjie.raft.core.transport.builder;

import com.tongbanjie.raft.core.exception.TransportException;
import com.tongbanjie.raft.core.transport.TransportClient;
import com.tongbanjie.raft.core.transport.netty.NettyClient;
import com.tongbanjie.raft.core.transport.netty.serialization.Serialization;
import com.tongbanjie.raft.core.transport.proxy.TransportClientProxy;
import com.tongbanjie.raft.core.util.NetUtil;
import org.apache.commons.lang.StringUtils;

/***
 *
 * @author banxia
 * @date 2017-12-04 16:16:09
 */
public class NettyClientBuilder<T> {


    //  host
    private String host;

    private int port;

    private Class<T> serviceInterface;

    private TransportClientProxy transportClientProxy;

    private Serialization serialization;

    private TransportClient transportClient;

    private long requestTimeout;

    public TransportClient getTransportClient() {
        return transportClient;
    }

    public NettyClientBuilder<T> host(String host) {
        this.host = host;
        return this;
    }

    public NettyClientBuilder<T> port(int port) {
        this.port = port;
        return this;
    }


    public NettyClientBuilder<T> serviceInterface(Class<T> serviceInterface) {
        this.serviceInterface = serviceInterface;
        return this;
    }

    public NettyClientBuilder<T> transportClientProxy(TransportClientProxy transportClientProxy) {
        this.transportClientProxy = transportClientProxy;
        return this;
    }

    public NettyClientBuilder<T> serialization(Serialization serialization) {
        this.serialization = serialization;
        return this;
    }


    public NettyClientBuilder<T> requestTimeout(long requestTimeout) {

        this.requestTimeout = requestTimeout;
        return this;
    }

    public T builder() {

        this.check();
        NettyClient<T> nettyClient = new NettyClient<T>();
        nettyClient.setHost(this.host);
        nettyClient.setPort(this.port);
        nettyClient.setRequestTimeout(this.requestTimeout);
        nettyClient.setServiceInterface(this.serviceInterface);
        nettyClient.setTransportClientProxy(this.transportClientProxy);
        nettyClient.setSerialization(this.serialization);
        nettyClient.open();
        this.transportClient = nettyClient;
        return nettyClient.getProxy(this.serviceInterface);
    }

    private void check() {

        if (StringUtils.isBlank(this.host)) {
            this.host = NetUtil.getLocalAddress().getHostAddress();
        }

        if (this.port <= 0) {
            throw new TransportException(String.format("port:%s not  allow", this.port));
        }

        if (null == serialization) {
            throw new TransportException("serialization not  allow null");
        }

        if (null == transportClientProxy) {
            throw new TransportException("transportClientProxy not  allow null");
        }
        if (null == serviceInterface) {
            throw new TransportException("serviceInterface not  allow null");
        }

    }


}
