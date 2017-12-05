package com.tongbanjie.raft.core.peer.support.server.impl;

import com.tongbanjie.raft.core.cmd.RaftCommand;
import com.tongbanjie.raft.core.enums.RaftCommandType;
import com.tongbanjie.raft.core.peer.RaftPeer;
import com.tongbanjie.raft.core.peer.support.server.RaftClientService;
import com.tongbanjie.raft.core.protocol.JoinResponse;

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

    @Override
    public JoinResponse joinCluster(String server) {

        RaftCommand raftCommand = new RaftCommand();
        raftCommand.setName("Join");
        raftCommand.setType(RaftCommandType.JOIN.getValue());
        raftCommand.setConnectStr(server);
        return this.raftPeer.joinCluster(raftCommand);
    }
}
