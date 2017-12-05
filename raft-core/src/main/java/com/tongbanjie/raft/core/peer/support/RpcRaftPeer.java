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


    private RaftEngine raftEngine;


    private String id;

    private String host;

    private int port;

    private long matchIndex;


    private TransportClient transportClient;

    private TransportServer transportServer;

    private TransportServer raftClientTransportServer;

    private RaftClientService raftClientService;

    private RaftService raftService;


    public RpcRaftPeer(String id) {

        this.id = id;
        String[] split = this.id.split(":");
        this.host = split[0];
        this.port = Integer.valueOf(split[1]);

    }

    /**
     * 启动 peer 引擎
     *
     * @return
     */
    public boolean bootstrap() {
        this.raftEngine.bootstrap();
        return true;
    }


    public RaftEngine getRaftEngine() {
        return raftEngine;
    }

    public void setRaftEngine(RaftEngine raftEngine) {
        this.raftEngine = raftEngine;
    }

    @Override
    public void setMatchIndex(long matchIndex) {

        this.matchIndex = matchIndex;
    }

    @Override
    public long getMatchIndex() {

        return matchIndex;
    }


    public String getId() {
        return id;
    }


    /**
     * 发起投票选举
     *
     * @param request 投票选举请求体
     * @return 投票选举响应实体
     */
    public ElectionResponse electionVote(ElectionRequest request) {


        return this.raftService.electionVote(request);
    }

    /**
     * 追加日志
     *
     * @param request 追加日志请求体
     * @return
     */
    public AppendEntriesResponse appendEntries(AppendEntriesRequest request) {

        return this.raftService.appendEntries(request);

    }


    /**
     * 选举处理
     *
     * @param electionRequest 投票选举请求实体
     * @return
     */
    public ElectionResponse electionVoteHandler(ElectionRequest electionRequest) {


        return this.raftEngine.electionVoteHandler(electionRequest);

    }

    /**
     * 追加日志处理
     *
     * @param request 追加日志请求实体
     * @return
     */
    public AppendEntriesResponse appendEntriesHandler(AppendEntriesRequest request) {

        return this.raftEngine.appendEntriesHandler(request);
    }

    public void commandHandler(RaftCommand command, LogApplyListener applyListener) {

        this.raftEngine.commandHandler(command, applyListener);
    }


    /**
     * 注册 raft client 服务
     *
     * @param host       ip
     * @param clientPort 本地客户端监听端口
     */
    @Override
    public void registerRaftClientTransportServer(String host, Integer clientPort) {

        if (this.raftClientTransportServer == null) {

            NettyServerBuilder<RaftClientService> builder = new NettyServerBuilder<RaftClientService>();
            this.raftClientTransportServer = builder.port(clientPort)
                    .host(host)
                    .ref(new RaftClientServiceImpl(this))
                    .serialization(new Hessian2Serialization())
                    .threadNum(Runtime.getRuntime().availableProcessors())
                    .builder();

        } else if (this.raftClientTransportServer.isClosed()) {
            this.raftClientTransportServer.open();
        }
    }

    /**
     * 注册 连接 raft 服务客户端
     */
    @Override
    public void registerRaftTransportClient() {

        if (this.transportClient == null) {
            NettyClientBuilder<RaftService> nettyClientBuilder = new NettyClientBuilder<RaftService>();
            this.raftService = nettyClientBuilder.port(this.port)
                    .host(this.host)
                    .serialization(new Hessian2Serialization())
                    .serviceInterface(RaftService.class)
                    .requestTimeout(6000)
                    .transportClientProxy(new JdkTransportClientProxy()).builder();
            this.transportClient = nettyClientBuilder.getTransportClient();
        }


    }

    /**
     * 注册 raft 服务
     */
    @Override
    public void registerRaftTransportServer() {

        if (this.transportServer == null) {

            NettyServerBuilder<RaftService> builder = new NettyServerBuilder<RaftService>();
            this.transportServer = builder.port(this.port)
                    .host(host)
                    .ref(new RaftServiceImpl(this))
                    .serialization(new Hessian2Serialization())
                    .threadNum(Runtime.getRuntime().availableProcessors())
                    .builder();
        } else if (this.transportServer.isClosed()) {
            this.transportServer.open();
        }
    }

    @Override
    public void unregisterRaftTransportClient() {
        if (this.transportClient != null && this.transportClient.isAvailable()) {
            try {

                this.transportClient.close();
            } catch (Exception e) {

            }
        }
    }

    /**
     * join the raft cluster
     *
     * @param raftCommand
     * @return
     */
    @Override
    public JoinResponse joinCluster(RaftCommand raftCommand) {

        //1.    check has leader
        //2.    not leader
        //3.    leader
        //  TODO
        return this.raftEngine.joinCluster(raftCommand);
    }


}
