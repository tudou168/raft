package com.tongbanjie.raft.core.peer;


import com.tongbanjie.raft.core.protocol.ElectionRequest;
import com.tongbanjie.raft.core.protocol.ElectionResponse;

/***
 *
 * @author banxia
 * @date 2017-11-15 17:17:02
 */
public interface RaftPeer {


    String getId();

    //  发起选举投票请求
    ElectionResponse electionVote(ElectionRequest request);
}
