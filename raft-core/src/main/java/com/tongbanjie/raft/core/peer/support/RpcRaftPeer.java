package com.tongbanjie.raft.core.peer.support;

import com.tongbanjie.raft.core.cmd.RaftCommand;
import com.tongbanjie.raft.core.engine.RaftEngine;
import com.tongbanjie.raft.core.listener.LogApplyListener;
import com.tongbanjie.raft.core.peer.RaftPeer;
import com.tongbanjie.raft.core.peer.support.server.RaftClientService;
import com.tongbanjie.raft.core.peer.support.server.impl.RaftClientServiceImpl;
import com.tongbanjie.raft.core.peer.support.server.RaftService;
import com.tongbanjie.raft.core.peer.support.server.impl.RaftServiceImpl;
import com.tongbanjie.raft.core.protocol.*;
import com.tongbanjie.raft.core.transport.TransportClient;
import com.tongbanjie.raft.core.transport.TransportServer;
import com.tongbanjie.raft.core.transport.builder.NettyClientBuilder;
import com.tongbanjie.raft.core.transport.builder.NettyServerBuilder;
import com.tongbanjie.raft.core.transport.netty.serialization.support.Hessian2Serialization;
import com.tongbanjie.raft.core.transport.proxy.support.JdkTransportClientProxy;

/***
 * 基于rpc方式的 raft peer
 * @author banxia
 * @date 2017-11-15 17:17:52
 */
public class RpcRaftPeer implements RaftPeer {


    private String id;

    private String host;

    private int port;

    private RaftService raftService;

    private TransportClient client;


    private long matchIndex;


    public RpcRaftPeer(String server) {
        this.id = server;
        String[] split = this.id.split(":");
        this.host = split[0];
        this.port = Integer.valueOf(split[1]);

        NettyClientBuilder<RaftService> nettyClientBuilder = new NettyClientBuilder<RaftService>();
        this.raftService = nettyClientBuilder.port(this.port)
                .host(this.host)
                .serialization(new Hessian2Serialization())
                .serviceInterface(RaftService.class)
                .requestTimeout(6000)
                .transportClientProxy(new JdkTransportClientProxy()).builder();
        this.client = nettyClientBuilder.getTransportClient();

    }


    public RaftService getRaftService() {
        return raftService;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public TransportClient getTransportClient() {
        return this.client;
    }

    @Override
    public void setMatchIndex(long matchIndex) {

        this.matchIndex = matchIndex;
    }

    @Override
    public long getMatchIndex() {
        return this.matchIndex;
    }
}
