package com.tongbanjie.raft.core.peer.support.server.impl;

import com.tongbanjie.raft.core.engine.RaftEngine;
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

    private RaftEngine raftEngine;

    public RaftServiceImpl(RaftEngine raftEngine) {
        this.raftEngine = raftEngine;
    }

    /**
     *  发起投票请求投票选举
     * @param request
     * @return
     */
    public ElectionResponse electionVote(ElectionRequest request) {
        return raftEngine.electionVoteHandler(request);
    }

    /**
     * 发起追加日志请求
     *
     * @param request
     * @return
     */
    public AppendEntriesResponse appendEntries(AppendEntriesRequest request) {

        return raftEngine.appendEntriesHandler(request);
    }


}
