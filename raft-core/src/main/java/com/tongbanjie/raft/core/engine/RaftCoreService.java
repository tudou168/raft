package com.tongbanjie.raft.core.engine;

import com.tongbanjie.raft.core.engine.handler.ElectionResponseHandler;
import com.tongbanjie.raft.core.engine.handler.ReplicationLogResponseHandler;
import com.tongbanjie.raft.core.log.manage.RaftLogService;
import com.tongbanjie.raft.core.peer.RaftPeer;
import com.tongbanjie.raft.core.peer.support.server.RaftClientService;
import com.tongbanjie.raft.core.peer.support.server.RaftService;
import com.tongbanjie.raft.core.peer.support.server.impl.RaftClientServiceImpl;
import com.tongbanjie.raft.core.peer.support.server.impl.RaftServiceImpl;
import com.tongbanjie.raft.core.protocol.*;
import com.tongbanjie.raft.core.transport.TransportClient;
import com.tongbanjie.raft.core.transport.TransportServer;
import com.tongbanjie.raft.core.transport.builder.NettyServerBuilder;
import com.tongbanjie.raft.core.transport.netty.serialization.support.Hessian2Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 *
 * @author banxia
 * @date 2017-12-11 11:11:29
 */
public class RaftCoreService {

    private final static Logger log = LoggerFactory.getLogger(RaftCoreService.class);

    private RaftEngine raftEngine;


    private TransportServer transportServer;

    private TransportServer transportClientService;

    public RaftCoreService(RaftEngine raftEngine) {
        this.raftEngine = raftEngine;
        String[] split = this.raftEngine.getId().split(":");
        NettyServerBuilder<RaftService> builder = new NettyServerBuilder<RaftService>();
        this.transportServer = builder.port(Integer.valueOf(split[1]))
                .host(split[0])
                .ref(new RaftServiceImpl(raftEngine))
                .serialization(new Hessian2Serialization())
                .threadNum(Runtime.getRuntime().availableProcessors())
                .builder();
    }


    public void registerRaftClientService(int clientPort) {

        NettyServerBuilder<RaftClientService> builder = new NettyServerBuilder<RaftClientService>();
        this.transportClientService = builder.port(clientPort)
                .ref(new RaftClientServiceImpl(raftEngine))
                .serialization(new Hessian2Serialization())
                .threadNum(Runtime.getRuntime().availableProcessors())
                .builder();

    }


    public void electionVoteRequest(RaftPeer raftPeer, ElectionRequest request, ElectionResponseHandler handler) {

        ElectionResponseTuple tuple = new ElectionResponseTuple();
        try {

            ElectionResponse electionResponse = raftPeer.getRaftService().electionVote(request);
            tuple.setElectionResponse(electionResponse);
            tuple.setId(raftPeer.getId());
            tuple.setSuccess(true);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tuple.setId(raftPeer.getId());
            tuple.setSuccess(false);
        }
        handler.handler(raftPeer, tuple);
    }


    /**
     * 并发复制日志
     *
     * @param peer
     * @param request
     * @param nextIndex
     * @param replicationLogResponseHandler
     */
    public void replication(RaftPeer peer, AppendEntriesRequest request, NextIndex nextIndex, ReplicationLogResponseHandler replicationLogResponseHandler) {


        ReplicationLogResponseTuple tuple = new ReplicationLogResponseTuple();
        try {

            AppendEntriesResponse response = peer.getRaftService().appendEntries(request);
            tuple.setAppendEntriesResponse(response);
            tuple.setSuccess(true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tuple.setReason(e.getMessage());
        }

        replicationLogResponseHandler.handler(peer, tuple, nextIndex);
    }
}
