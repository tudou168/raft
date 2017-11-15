package com.tongbanjie.raft.core.peer.support;

import com.tongbanjie.raft.core.election.ElectionVoteRequest;
import com.tongbanjie.raft.core.election.ElectionVoteResponse;
import com.tongbanjie.raft.core.peer.RaftPeer;

/***
 * 基于rpc方式的 raft peer
 * @author banxia
 * @date 2017-11-15 17:17:52
 */
public class RpcRaftPeer implements RaftPeer {
    public String getId() {
        return null;
    }

    /**
     * 发起投票选举
     *
     * @param request 投票选举请求体
     * @return 投票选举响应实体
     */
    public ElectionVoteResponse electionVote(ElectionVoteRequest request) {
        return null;
    }
}
