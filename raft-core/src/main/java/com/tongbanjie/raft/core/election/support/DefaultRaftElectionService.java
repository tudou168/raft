package com.tongbanjie.raft.core.election.support;

import com.tongbanjie.raft.core.election.RaftElectionService;
import com.tongbanjie.raft.core.election.handler.ElectionResponseHandler;
import com.tongbanjie.raft.core.peer.RaftPeer;
import com.tongbanjie.raft.core.protocol.ElectionRequest;

/***
 * 带有投票超时功能的选举
 * @author banxia
 * @date 2017-11-15 20:20:31
 */
public class DefaultRaftElectionService implements RaftElectionService {


    public void electionVoteRequest(RaftPeer raftPeer, ElectionRequest request, ElectionResponseHandler handler) {

        //TODO
    }
}
