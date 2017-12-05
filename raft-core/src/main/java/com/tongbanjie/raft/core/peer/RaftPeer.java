package com.tongbanjie.raft.core.peer;


import com.tongbanjie.raft.core.cmd.RaftCommand;
import com.tongbanjie.raft.core.engine.RaftEngine;
import com.tongbanjie.raft.core.listener.LogApplyListener;
import com.tongbanjie.raft.core.protocol.*;

/***
 *
 * @author banxia
 * @date 2017-11-15 17:17:02
 */
public interface RaftPeer {


    String getId();


    void setRaftEngine(RaftEngine raftEngine);

    void setMatchIndex(long matchIndex);

    long getMatchIndex();

    boolean bootstrap();

    //  发起选举投票请求
    ElectionResponse electionVote(ElectionRequest request);

    /**
     * 追加日志
     *
     * @param request 追加日志请求体
     * @return
     */
    AppendEntriesResponse appendEntries(AppendEntriesRequest request);

    /**
     * 选举处理
     *
     * @param electionRequest
     * @return
     */
    ElectionResponse electionVoteHandler(ElectionRequest electionRequest);

    /**
     * 追加日志
     *
     * @param appendEntriesRequest
     * @return
     */
    AppendEntriesResponse appendEntriesHandler(AppendEntriesRequest appendEntriesRequest);


    /**
     * 执行命令
     *
     * @param command
     * @param applyListener
     */
    void commandHandler(RaftCommand command, LogApplyListener applyListener);


    void registerRaftClientTransportServer(String host, Integer clientPort);

    void registerRaftTransportClient();

    void registerRaftTransportServer();

    void unregisterRaftTransportClient();

    /**
     * 加入集群
     *
     * @param raftCommand
     * @return
     */
    JoinResponse joinCluster(RaftCommand raftCommand);
}
