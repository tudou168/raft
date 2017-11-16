package com.tongbanjie.raft.core.peer.support;

import com.tongbanjie.raft.core.peer.RaftPeer;
import com.tongbanjie.raft.core.protocol.ElectionRequest;
import com.tongbanjie.raft.core.protocol.ElectionResponse;

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
    public ElectionResponse electionVote(ElectionRequest request) {
        return null;
    }
}
