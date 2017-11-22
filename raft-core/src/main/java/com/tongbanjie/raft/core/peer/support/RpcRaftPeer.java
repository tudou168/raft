package com.tongbanjie.raft.core.peer.support;

import com.alibaba.fastjson.JSON;
import com.tongbanjie.raft.core.engine.RaftEngine;
import com.tongbanjie.raft.core.enums.RemotingCommandState;
import com.tongbanjie.raft.core.enums.RemotingCommandType;
import com.tongbanjie.raft.core.exception.RaftException;
import com.tongbanjie.raft.core.peer.RaftPeer;
import com.tongbanjie.raft.core.protocol.AppendEntriesRequest;
import com.tongbanjie.raft.core.protocol.AppendEntriesResponse;
import com.tongbanjie.raft.core.protocol.ElectionRequest;
import com.tongbanjie.raft.core.protocol.ElectionResponse;
import com.tongbanjie.raft.core.remoting.RemotingClient;
import com.tongbanjie.raft.core.remoting.RemotingCommand;
import com.tongbanjie.raft.core.remoting.RemotingServer;
import com.tongbanjie.raft.core.remoting.netty.NettyClient;
import com.tongbanjie.raft.core.remoting.netty.NettyServer;
import com.tongbanjie.raft.core.remoting.netty.RemotingCommandProcessor;
import com.tongbanjie.raft.core.util.RequestIdGenerator;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/***
 * 基于rpc方式的 raft peer
 * @author banxia
 * @date 2017-11-15 17:17:52
 */
public class RpcRaftPeer implements RaftPeer {


    private Random random = new Random();

    private RaftEngine raftEngine;


    private RemotingServer remotingServer;


    private RemotingClient remotingClient;

    private String id;

    private String host;

    private int port;

    public RpcRaftPeer(String id) {

        this.id = id;
        String[] split = this.id.split(":");
        this.host = split[0];
        this.port = Integer.valueOf(split[1]);


    }

    private void startNettyServer() {

        this.remotingServer = new NettyServer(new RemotingCommandProcessor(this));
        boolean open = this.remotingServer.open(host, port);
        if (!open) {

            throw new RaftException("raft peer init fail...");
        }
    }

    public RaftEngine getRaftEngine() {
        return raftEngine;
    }

    public void setRaftEngine(RaftEngine raftEngine) {
        this.raftEngine = raftEngine;
    }

    public void registerServer() {

        this.startNettyServer();
    }

    public String getId() {
        return id;
    }

    public RemotingClient getRemotingClient() {


        if (remotingClient == null || remotingClient.isClosed()) {
            remotingClient = new NettyClient();
            String[] split = this.id.split(":");
            String host = split[0];
            int port = Integer.valueOf(split[1]);
            boolean open = remotingClient.open(host, port);
            if (!open) {
                throw new RaftException("the netty client open fail");
            }
        }
        return this.remotingClient;
    }

    public RemotingServer getRemotingServer() {
        return this.remotingServer;
    }

    /**
     * 发起投票选举
     *
     * @param request 投票选举请求体
     * @return 投票选举响应实体
     */
    public ElectionResponse electionVote(ElectionRequest request) {

        RemotingClient remotingClient = this.getRemotingClient();

        long requestId = RequestIdGenerator.getRequestId();
        RemotingCommand command = new RemotingCommand();
        command.setRequestId(requestId);
        command.setCommandType(RemotingCommandType.ELECTION.getValue());
        command.setBody(JSON.toJSONString(request));
        command.setState(RemotingCommandState.SUCCESS.getValue());
        RemotingCommand electionResponse = remotingClient.request(command);
        System.err.println("electionResponse------>>>>>>>>>>>");
        ElectionResponse response = new ElectionResponse();
        response.setTerm(request.getTerm());
        int value = random.nextInt(100);
        if (value % 2 == 0) {
            response.setVoteGranted(true);
        } else {
            response.setReason(String.format("request vote %s random num not match", request));
        }
        return response;
    }

    public AppendEntriesResponse appendEntries(AppendEntriesRequest request) {
        AppendEntriesResponse response = new AppendEntriesResponse();
        int value = random.nextInt(1000);

        long term = request.getTerm();
        response.setTerm(term);
        if (value % 2 == 0) {
            response.setSuccess(true);
        } else {

            if (value % 5 == 0) {
                long myTerm = request.getTerm() + 1;
                response.setTerm(myTerm);
                response.setReason(String.format("me.term %s > request.term %s", myTerm, request.getTerm()));
            } else {
                response.setReason("random num not match ");
            }

        }
        return response;
    }

    public ElectionResponse electionVoteHandler(ElectionRequest electionRequest) {

        ElectionResponse response = new ElectionResponse();
        response.setTerm(electionRequest.getTerm());
        int value = random.nextInt(100);
        if (value % 2 == 0) {
            response.setVoteGranted(true);
        } else {
            response.setReason(String.format("request vote %s random num not match", electionRequest));
        }
        return response;
    }

    public AppendEntriesResponse appendEntriesHandler(AppendEntriesRequest request) {
        int value = random.nextInt(1000);

        AppendEntriesResponse response = new AppendEntriesResponse();
        
        long term = request.getTerm();
        response.setTerm(term);
        if (value % 2 == 0) {
            response.setSuccess(true);
        } else {

            if (value % 5 == 0) {
                long myTerm = request.getTerm() + 1;
                response.setTerm(myTerm);
                response.setReason(String.format("me.term %s > request.term %s", myTerm, request.getTerm()));
            } else {
                response.setReason("random num not match ");
            }

        }
        return response;
    }


}
