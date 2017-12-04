package com.tongbanjie.raft.core.peer.support.server;

import com.tongbanjie.raft.core.protocol.AppendEntriesRequest;
import com.tongbanjie.raft.core.protocol.AppendEntriesResponse;
import com.tongbanjie.raft.core.protocol.ElectionRequest;
import com.tongbanjie.raft.core.protocol.ElectionResponse;

/***
 *
 * @author banxia
 * @date 2017-12-04 18:18:56
 */
public interface RaftService {
    /**
     * 投票选举
     *
     * @param request
     * @return
     */
    ElectionResponse electionVote(ElectionRequest request);

    /**
     * 复制日志
     *
     * @param request
     * @return
     */
    AppendEntriesResponse appendEntries(AppendEntriesRequest request);
}
