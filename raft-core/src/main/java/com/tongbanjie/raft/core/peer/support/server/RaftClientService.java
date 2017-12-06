package com.tongbanjie.raft.core.peer.support.server;

import com.tongbanjie.raft.core.protocol.JoinResponse;
import com.tongbanjie.raft.core.protocol.LeaveResponse;

/***
 *
 * @author banxia
 * @date 2017-12-04 17:17:59
 */
public interface RaftClientService {
    /**
     * 加入集群
     * @param server
     * @return
     */
    JoinResponse joinCluster(String server);

    /**
     * 脱离集群
     *
     * @param server
     * @return
     */
    LeaveResponse leaveCluster(String server);
}
