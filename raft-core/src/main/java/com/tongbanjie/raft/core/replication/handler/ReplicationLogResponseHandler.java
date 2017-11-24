package com.tongbanjie.raft.core.replication.handler;

import com.tongbanjie.raft.core.engine.NextIndex;
import com.tongbanjie.raft.core.peer.RaftPeer;
import com.tongbanjie.raft.core.protocol.ReplicationLogResponseTuple;

/***
 *
 * @author banxia
 * @date 2017-11-19 11:11:32
 */
public interface ReplicationLogResponseHandler {

    void handler(RaftPeer peer, ReplicationLogResponseTuple tuple, NextIndex nextIndex, Long staticsId);
}
