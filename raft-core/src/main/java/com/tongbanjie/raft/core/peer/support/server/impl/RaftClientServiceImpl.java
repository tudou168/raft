package com.tongbanjie.raft.core.peer.support.server.impl;

import com.tongbanjie.raft.core.peer.RaftPeer;
import com.tongbanjie.raft.core.peer.support.server.RaftClientService;

/***
 *
 * @author banxia
 * @date 2017-12-04 18:18:00
 */
public class RaftClientServiceImpl implements RaftClientService {

    private RaftPeer raftPeer;

    public RaftClientServiceImpl(RaftPeer raftPeer) {
        this.raftPeer = raftPeer;
    }
}
