package com.tongbanjie.raft.core.peer;

import com.tongbanjie.raft.core.election.ElectionVoteRequest;
import com.tongbanjie.raft.core.election.ElectionVoteResponse;

/***
 *
 * @author banxia
 * @date 2017-11-15 17:17:02
 */
public interface RaftPeer {

    String getId();

    //  发起选举投票请求
    ElectionVoteResponse electionVote(ElectionVoteRequest request);
}
