package com.tongbanjie.raft.core.peer.support.server.impl;

import com.tongbanjie.raft.core.cmd.RaftCommand;
import com.tongbanjie.raft.core.constant.RaftConstant;
import com.tongbanjie.raft.core.enums.RaftCommandType;
import com.tongbanjie.raft.core.peer.RaftPeer;
import com.tongbanjie.raft.core.peer.support.server.RaftClientService;
import com.tongbanjie.raft.core.protocol.JoinResponse;
import com.tongbanjie.raft.core.protocol.LeaveResponse;
import org.apache.commons.lang.StringUtils;

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


        JoinResponse joinResponse = new JoinResponse();


        String peerId = raftPeer.getId();
        String leader = this.raftPeer.getRaftEngine().getLeader();

        RaftCommand raftCommand = new RaftCommand();
        raftCommand.setName("Join");
        raftCommand.setType(RaftCommandType.JOIN.getValue());
        raftCommand.setConnectStr(server);


        //  eq leader ?
        if (StringUtils.equals(peerId, leader)) {
            return this.raftPeer.joinCluster(raftCommand);
        } else if (StringUtils.equals(leader, RaftConstant.noLeader)) {
            // not found leader ?
            joinResponse.setReason("has not leader error!");
        } else {
            joinResponse.setReason(server + " is not leader node error");
        }

//        joinResponse.setReason("操作失败");
        return joinResponse;


    }

    @Override
    public LeaveResponse leaveCluster(String server) {
        String peerId = raftPeer.getId();
        String leader = this.raftPeer.getRaftEngine().getLeader();

        RaftCommand raftCommand = new RaftCommand();
        raftCommand.setName("Leave");
        raftCommand.setType(RaftCommandType.LEAVE.getValue());
        raftCommand.setConnectStr(server);
        LeaveResponse leaveResponse = new LeaveResponse();

        //  eq leader ?
        if (StringUtils.equals(peerId, leader)) {

            return this.raftPeer.leaveCluster(raftCommand);

        } else if (StringUtils.equals(leader, RaftConstant.noLeader)) {
            // not found leader ?
            leaveResponse.setReason("has not leader error!");
        } else {
            leaveResponse.setReason(server + " is not leader node error");
        }

//        joinResponse.setReason("操作失败");
        return leaveResponse;

    }
}
