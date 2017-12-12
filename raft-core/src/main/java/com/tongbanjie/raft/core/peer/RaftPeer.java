package com.tongbanjie.raft.core.peer;


import com.tongbanjie.raft.core.cmd.RaftCommand;
import com.tongbanjie.raft.core.engine.RaftEngine;
import com.tongbanjie.raft.core.listener.LogApplyListener;
import com.tongbanjie.raft.core.peer.support.server.RaftService;
import com.tongbanjie.raft.core.protocol.*;
import com.tongbanjie.raft.core.transport.TransportClient;

/***
 *
 * @author banxia
 * @date 2017-11-15 17:17:02
 */
public interface RaftPeer {


    /**
     * id
     *
     * @return
     */
    String getId();

    /**
     * raft 服务
     *
     * @return
     */
    RaftService getRaftService();

    /**
     * transport client
     *
     * @return
     */
    TransportClient getTransportClient();

    /**
     * match index
     *
     * @param matchIndex
     */
    void setMatchIndex(long matchIndex);

    /**
     * match index
     *
     * @return
     */
    long getMatchIndex();
}
