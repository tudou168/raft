package com.tongbanjie.raft.core.replication;

import com.tongbanjie.raft.core.engine.NextIndex;
import com.tongbanjie.raft.core.peer.RaftPeer;
import com.tongbanjie.raft.core.protocol.AppendEntriesRequest;
import com.tongbanjie.raft.core.replication.handler.ReplicationLogResponseHandler;

/***
 *
 * @author banxia
 * @date 2017-11-19 11:11:28
 */
public interface ReplicationService {

    void replication(RaftPeer peer, AppendEntriesRequest request, NextIndex nextIndex, ReplicationLogResponseHandler replicationLogResponseHandler, Long staticsId);
}
