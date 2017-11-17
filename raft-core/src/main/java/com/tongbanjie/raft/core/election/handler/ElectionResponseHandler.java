package com.tongbanjie.raft.core.election.handler;

import com.tongbanjie.raft.core.peer.RaftPeer;
import com.tongbanjie.raft.core.protocol.ElectionResponseTuple;

/***
 *
 * @author banxia
 * @date 2017-11-15 20:20:07
 */
public interface ElectionResponseHandler {


    void handler(RaftPeer raftPeer, ElectionResponseTuple tuple);
}
