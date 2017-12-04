package com.tongbanjie.raft.core.peer.support.server.impl;

import com.tongbanjie.raft.core.peer.RaftPeer;
import com.tongbanjie.raft.core.peer.support.server.RaftService;
import com.tongbanjie.raft.core.protocol.AppendEntriesRequest;
import com.tongbanjie.raft.core.protocol.AppendEntriesResponse;
import com.tongbanjie.raft.core.protocol.ElectionRequest;
import com.tongbanjie.raft.core.protocol.ElectionResponse;

/***
 *
 * @author banxia
 * @date 2017-12-04 18:18:56
 */
public class RaftServiceImpl implements RaftService {

    private RaftPeer raftPeer;

    public RaftServiceImpl(RaftPeer raftPeer) {
        this.raftPeer = raftPeer;
    }

    /**
     * 投票选举
     *
     * @param request
     * @return
     */
    @Override
    public ElectionResponse electionVote(ElectionRequest request) {
        return raftPeer.electionVoteHandler(request);
    }

    @Override
    public AppendEntriesResponse appendEntries(AppendEntriesRequest request) {
        return raftPeer.appendEntriesHandler(request);
    }
}
