package com.tongbanjie.raft.core.replication.support;

import com.tongbanjie.raft.core.engine.NextIndex;
import com.tongbanjie.raft.core.engine.RaftEngine;
import com.tongbanjie.raft.core.peer.RaftPeer;
import com.tongbanjie.raft.core.protocol.AppendEntriesRequest;
import com.tongbanjie.raft.core.protocol.AppendEntriesResponse;
import com.tongbanjie.raft.core.protocol.RaftLog;
import com.tongbanjie.raft.core.protocol.ReplicationLogResponseTuple;
import com.tongbanjie.raft.core.replication.ReplicationService;
import com.tongbanjie.raft.core.replication.handler.ReplicationLogResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/***
 *
 * @author banxia
 * @date 2017-11-19 11:11:28
 */
public class DefaultReplicationService implements ReplicationService {


    private final static Logger log = LoggerFactory.getLogger(DefaultReplicationService.class);


    public void replication(RaftPeer peer, AppendEntriesRequest request, NextIndex nextIndex, ReplicationLogResponseHandler replicationLogResponseHandler, Long staticsId) {


        ReplicationLogResponseTuple tuple = new ReplicationLogResponseTuple();
        try {

            AppendEntriesResponse response = peer.appendEntries(request);
            tuple.setAppendEntriesResponse(response);
            tuple.setSuccess(true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            tuple.setReason(e.getMessage());
        }

        replicationLogResponseHandler.handler(peer, tuple, nextIndex, staticsId);
    }
}
