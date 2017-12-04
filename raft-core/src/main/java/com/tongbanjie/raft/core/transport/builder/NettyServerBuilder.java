package com.tongbanjie.raft.core.transport.builder;

import com.tongbanjie.raft.core.exception.TransportException;
import com.tongbanjie.raft.core.transport.TransportServer;
import com.tongbanjie.raft.core.transport.netty.NettyServer;
import com.tongbanjie.raft.core.transport.netty.serialization.Serialization;
import com.tongbanjie.raft.core.util.NetUtil;
import org.apache.commons.lang.StringUtils;

/***
 * netty server builder
 * @author banxia
 * @date 2017-12-04 15:15:37
 */
public class NettyServerBuilder<T> {


    //  host
    private String host;

    //  port
    private int port;

    //  serialization
    private Serialization serialization;

    private T ref;

    private int threadNum = Runtime.getRuntime().availableProcessors();


    public NettyServerBuilder<T> host(String host) {
        this.host = host;
        return this;
    }

    public NettyServerBuilder<T> port(int port) {
        this.port = port;
        return this;
    }

    public NettyServerBuilder<T> serialization(Serialization serialization) {
        this.serialization = serialization;
        return this;
    }


    public NettyServerBuilder<T> threadNum(int threadNum) {
        this.threadNum = threadNum;
        return this;
    }

    public NettyServerBuilder<T> ref(T ref) {

        this.ref = ref;
        return this;
    }


    public TransportServer builder() {

        this.check();
        NettyServer<T> nettyServer = new NettyServer<T>();
        nettyServer.setHost(host);
        nettyServer.setPort(port);
        nettyServer.setThreadNum(threadNum);
        nettyServer.setSerialization(serialization);
        nettyServer.setRef(ref);
        nettyServer.open();
        return nettyServer;
    }


    private void check() {

        if (StringUtils.isBlank(this.host)) {
            this.host = NetUtil.getLocalAddress().getHostAddress();
        }

        if (this.port <= 0) {
            throw new TransportException(String.format("port:%s not  allow", this.port));
        }

        if (this.threadNum <= 0) {
            throw new TransportException(String.format("threadNum:%s not  allow", this.threadNum));
        }

        if (null == serialization) {
            throw new TransportException("serialization not  allow null");
        }

        if (null == ref) {
            throw new TransportException("ref not  allow null");
        }

        Class<?>[] interfaces = this.ref.getClass().getInterfaces();
        if (null == interfaces || interfaces.length == 0) {
            throw new TransportException("ref has not impl a interface");
        }

    }

}
