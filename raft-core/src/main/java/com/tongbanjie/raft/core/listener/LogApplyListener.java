package com.tongbanjie.raft.core.listener;

import com.tongbanjie.raft.core.protocol.RaftLog;

/***
 *  apply listener
 *  the client set value  callback response notify
 * @author banxia
 * @date 2017-11-24 14:14:29
 */
public interface LogApplyListener {


    /**
     * notify
     *
     * @param commitIndex 提交索引
     * @param raftLog     raft 日志
     */
    void notify(long commitIndex, RaftLog raftLog);

}
