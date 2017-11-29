package com.tongbanjie.raft.core.peer;


import com.tongbanjie.raft.core.engine.RaftEngine;
import com.tongbanjie.raft.core.protocol.AppendEntriesRequest;
import com.tongbanjie.raft.core.protocol.AppendEntriesResponse;
import com.tongbanjie.raft.core.protocol.ElectionRequest;
import com.tongbanjie.raft.core.protocol.ElectionResponse;
import com.tongbanjie.raft.core.remoting.RemotingClient;
import com.tongbanjie.raft.core.remoting.RemotingServer;

/***
 *
 * @author banxia
 * @date 2017-11-15 17:17:02
 */
public interface RaftPeer {


    String getId();


    void setRaftEngine(RaftEngine raftEngine);


    RaftEngine getRaftEngine();


    void setRemotingServer(RemotingServer remotingServer);


    boolean bootstrap();


    /**
     * 注册服务
     */
    void registerServer();

    /**
     * 取消注册服务
     */
    void unregisterServer();


    void registerRemotingClient();

    void unregisterRemotingClient();

    //  发起选举投票请求
    ElectionResponse electionVote(ElectionRequest request);

    /**
     * 追加日志
     * @param request 追加日志请求体
     * @return
     */
    AppendEntriesResponse appendEntries(AppendEntriesRequest request);

    /**
     * 选举处理
     * @param electionRequest
     * @return
     */
    ElectionResponse electionVoteHandler(ElectionRequest electionRequest);

    /**
     * 追加日志
     * @param appendEntriesRequest
     * @return
     */
    AppendEntriesResponse appendEntriesHandler(AppendEntriesRequest appendEntriesRequest);
}
