package com.tongbanjie.raft.core.log.manage;

import com.tongbanjie.raft.core.protocol.RaftLog;

import java.util.List;

/***
 *raft 日志管理
 * @author banxia
 * @date 2017-11-15 10:10:16
 */
public interface RaftLogManage {


    /**
     * 是否包含指定的日志
     *
     * @param index 日志索引
     * @param term  任期
     * @return
     */
    boolean contains(long index, long term);

    /**
     * 获取指定日志索引之后的所有日志列表
     *
     * @param index 日志索引
     * @return
     */
    List<RaftLog> getRaftLogListFromIndex(long index);

    /**
     * 截断raft日志
     * 此方法对于follower获取追加日志的请求的时候，会首先把leader传递过来的 index 和 term 之后的本地未提交的日志进行截断删除操作，
     * 然后进行将leader的日志追加到本地。
     *
     * @param index 日志索引
     * @param term  任期
     * @return
     */
    boolean truncateRaftLog(long index, long term);

    /**
     * 追加日志
     *
     * @param raftLog raft 日志实体
     * @return
     */
    boolean appendRaftLog(RaftLog raftLog);

    /**
     * 将未提交的日志提交到指定的 index
     *
     * @param commitIndex 提交到指定的日志 index
     * @return
     */
    boolean commitToIndex(long commitIndex);


    /**
     * raft log 列表中最后的索引
     *
     * @return
     */
    long getLastIndex();

    /**
     * raft log 列表中最后的任期值
     *
     * @return
     */
    long getLastTerm();

    /**
     * raft log  最后提交的索引
     *
     * @return
     */
    long getLastCommittedIndex();


    /**
     * 获取日志列表
     *
     * @return
     */
    List<RaftLog> getRaftLogList();


}
