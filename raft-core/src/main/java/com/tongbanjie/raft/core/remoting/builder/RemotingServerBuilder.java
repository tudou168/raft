package com.tongbanjie.raft.core.remoting.builder;

import com.tongbanjie.raft.core.remoting.RemotingChannel;
import com.tongbanjie.raft.core.remoting.RemotingServer;
import com.tongbanjie.raft.core.remoting.support.netty.NettyServer;
import com.tongbanjie.raft.core.remoting.support.netty.RemotingCommandProcessor;

/***
 *
 * @author banxia
 * @date 2017-11-28 10:10:57
 */
public class RemotingServerBuilder {

    private String host = "127.0.0.1";

    private int port = 9876;

    private RemotingCommandProcessor remotingCommandProcessor;

    private RemotingServer remotingServer;


    public RemotingServerBuilder host(String host) {
        this.host = host;
        return this;
    }

    public RemotingServerBuilder port(int port) {
        this.port = port;
        return this;
    }

    public RemotingServerBuilder remotingCommandProcessor(RemotingCommandProcessor remotingCommandProcessor) {
        this.remotingCommandProcessor = remotingCommandProcessor;
        return this;
    }


    public RemotingServer builder() {

        if (remotingCommandProcessor == null) {

            throw new RuntimeException("remotingCommandProcessor not allow null!");
        }

        NettyServer nettyServer = new NettyServer();
        nettyServer.setHost(this.host);
        nettyServer.setPort(port);
        nettyServer.setCommandProcessor(this.remotingCommandProcessor);

        this.remotingServer = nettyServer;
        this.remotingServer.open();
        return this.remotingServer;

    }


}
