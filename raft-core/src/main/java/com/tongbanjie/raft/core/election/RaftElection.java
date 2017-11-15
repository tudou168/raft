package com.tongbanjie.raft.core.election;

import com.tongbanjie.raft.core.election.handler.ElectionVoteResponseHandler;
import com.tongbanjie.raft.core.peer.RaftPeer;

/***
 * raft 选举
 * @author banxia
 * @date 2017-11-15 19:19:32
 */
public interface RaftElection {


    /**
     * 投票选举请求
     *
     * @param raftPeer
     * @param request
     * @param handler
     */
    void electionVoteRequest(RaftPeer raftPeer, ElectionVoteRequest request, ElectionVoteResponseHandler handler);

}
