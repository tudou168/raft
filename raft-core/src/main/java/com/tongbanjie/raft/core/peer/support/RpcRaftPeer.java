package com.tongbanjie.raft.core.peer.support;

import com.tongbanjie.raft.core.engine.RaftEngine;
import com.tongbanjie.raft.core.peer.RaftPeer;
import com.tongbanjie.raft.core.protocol.ElectionRequest;
import com.tongbanjie.raft.core.protocol.ElectionResponse;

import java.util.Random;

/***
 * 基于rpc方式的 raft peer
 * @author banxia
 * @date 2017-11-15 17:17:52
 */
public class RpcRaftPeer implements RaftPeer {


    private Random random = new Random();

    private RaftEngine raftEngine;

    public RpcRaftPeer(RaftEngine raftEngine) {
        this.raftEngine = raftEngine;
    }

    public String getId() {
        return raftEngine.getId();
    }

    /**
     * 发起投票选举
     *
     * @param request 投票选举请求体
     * @return 投票选举响应实体
     */
    public ElectionResponse electionVote(ElectionRequest request) {

        ElectionResponse response = new ElectionResponse();
        response.setTerm(request.getTerm());
        int value = random.nextInt(100);
        if (value % 2 == 0) {
            response.setVoteGranted(true);
        } else {
            response.setReason("TODO");
        }
        return response;
    }
}
