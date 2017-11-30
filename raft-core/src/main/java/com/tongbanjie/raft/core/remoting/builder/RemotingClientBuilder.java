package com.tongbanjie.raft.core.remoting.builder;

import com.tongbanjie.raft.core.remoting.RemotingChannel;
import com.tongbanjie.raft.core.remoting.RemotingClient;
import com.tongbanjie.raft.core.remoting.support.netty.NettyClient;
import org.apache.commons.lang.StringUtils;

/***
 *
 * @author banxia
 * @date 2017-11-28 10:10:45
 */
public class RemotingClientBuilder {


    //  host
    private String host;
    // port
    private int port = 8080;
    // request timeout
    private int requestTimeout = 3000;

    private RemotingClient remotingClient;


    public RemotingClientBuilder host(String host) {
        this.host = host;
        return this;
    }

    public RemotingClientBuilder port(int port) {
        this.port = port;
        return this;
    }

    public RemotingClientBuilder requestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
        return this;
    }


    public RemotingClient builder() {

        if (StringUtils.isBlank(host)) {
            throw new RuntimeException("host not allow null!");
        }

        NettyClient nettyClient = new NettyClient();
        nettyClient.setHost(this.host);
        nettyClient.setPort(port);
        nettyClient.setRequestTimeout(requestTimeout);
        this.remotingClient = nettyClient;
        this.remotingClient.open();
        return this.remotingClient;

    }


}
