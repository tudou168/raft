package com.tongbanjie.raft.core.election.support;

import com.tongbanjie.raft.core.election.ElectionVoteRequest;
import com.tongbanjie.raft.core.election.RaftElection;
import com.tongbanjie.raft.core.election.handler.ElectionVoteResponseHandler;
import com.tongbanjie.raft.core.peer.RaftPeer;

/***
 * 带有投票超时功能的选举
 * @author banxia
 * @date 2017-11-15 20:20:31
 */
public class RaftElectionWithTimeout implements RaftElection {


    public void electionVoteRequest(RaftPeer raftPeer, ElectionVoteRequest request, ElectionVoteResponseHandler handler) {

        //TODO
    }
}
