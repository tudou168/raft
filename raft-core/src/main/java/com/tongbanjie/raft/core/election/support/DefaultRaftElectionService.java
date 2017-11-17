package com.tongbanjie.raft.core.election.support;

import com.tongbanjie.raft.core.election.RaftElectionService;
import com.tongbanjie.raft.core.election.handler.ElectionResponseHandler;
import com.tongbanjie.raft.core.engine.RaftEngine;
import com.tongbanjie.raft.core.peer.RaftPeer;
import com.tongbanjie.raft.core.protocol.ElectionRequest;
import com.tongbanjie.raft.core.protocol.ElectionResponse;
import com.tongbanjie.raft.core.protocol.ElectionResponseTuple;

/***
 * 带有投票超时功能的选举
 * @author banxia
 * @date 2017-11-15 20:20:31
 */
public class DefaultRaftElectionService implements RaftElectionService {

    private RaftEngine engine;


    public DefaultRaftElectionService(RaftEngine engine) {
        this.engine = engine;
    }

    public void electionVoteRequest(RaftPeer raftPeer, ElectionRequest request, ElectionResponseHandler handler) {

        ElectionResponseTuple tuple = new ElectionResponseTuple();
        try {

            ElectionResponse electionResponse = raftPeer.electionVote(request);
            tuple.setElectionResponse(electionResponse);
            tuple.setId(raftPeer.getId());
            tuple.setSuccess(true);

        } catch (Exception e) {
            e.printStackTrace();
            tuple.setSuccess(false);
        }
        handler.handler(raftPeer, tuple);
    }
}
