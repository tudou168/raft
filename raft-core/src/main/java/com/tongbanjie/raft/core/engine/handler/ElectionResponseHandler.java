package com.tongbanjie.raft.core.engine.handler;

import com.tongbanjie.raft.core.peer.RaftPeer;
import com.tongbanjie.raft.core.protocol.ElectionResponseTuple;

/***
 *
 * @author banxia
 * @date 2017-11-15 20:20:07
 */
public interface ElectionResponseHandler {


    /**
     * 选举响应
     *
     * @param raftPeer
     * @param tuple
     */
    void handler(RaftPeer raftPeer, ElectionResponseTuple tuple);
}
