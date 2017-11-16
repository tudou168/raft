package com.tongbanjie.raft.core.election;

import com.tongbanjie.raft.core.election.handler.ElectionResponseHandler;
import com.tongbanjie.raft.core.peer.RaftPeer;
import com.tongbanjie.raft.core.protocol.ElectionRequest;

/***
 * raft 选举
 * @author banxia
 * @date 2017-11-15 19:19:32
 */
public interface RaftElectionService {


    /**
     * 投票选举
     * @param raftPeer
     * @param request
     * @param handler
     */
    void electionVoteRequest(RaftPeer raftPeer, ElectionRequest request, ElectionResponseHandler handler);

}
