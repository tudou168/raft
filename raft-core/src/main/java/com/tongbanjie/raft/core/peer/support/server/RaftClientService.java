package com.tongbanjie.raft.core.peer.support.server;

import com.tongbanjie.raft.core.protocol.JoinResponse;

/***
 *
 * @author banxia
 * @date 2017-12-04 17:17:59
 */
public interface RaftClientService {
    /**
     * @param server
     * @return
     */
    JoinResponse joinCluster(String server);
}
