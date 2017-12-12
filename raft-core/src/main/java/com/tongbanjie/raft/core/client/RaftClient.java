package com.tongbanjie.raft.core.client;

import com.tongbanjie.raft.core.peer.support.server.RaftClientService;
import com.tongbanjie.raft.core.protocol.JoinResponse;
import com.tongbanjie.raft.core.protocol.LeaveResponse;
import com.tongbanjie.raft.core.transport.TransportClient;

/***
 *
 * @author banxia
 * @date 2017-12-05 14:14:32
 */
public class RaftClient {

    private RaftClientService raftClientService;

    private TransportClient transportClient;

    public RaftClient(RaftClientService raftClientService) {
        this.raftClientService = raftClientService;
    }

    public RaftClient(RaftClientService raftClientService, TransportClient transportClient) {
        this.raftClientService = raftClientService;
        this.transportClient = transportClient;
    }

    public void close() {

        if (transportClient != null) {

            this.transportClient.close();
        }

    }

    /**
     * 加入集群
     *
     * @param server ip:port
     */
    public JoinResponse joinCluster(String server) {

        return this.raftClientService.joinCluster(server);
    }

    /**
     * 脱离集群
     *
     * @param server ip:port
     */
    public LeaveResponse leaveCluster(String server) {
        return this.raftClientService.leaveCluster(server);
    }
}
