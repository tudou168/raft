package com.tongbanjie.raft.core.protocol;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;
import java.util.List;

/***
 * 追加日志请求实体
 * @author banxia
 * @date 2017-11-19 11:11:16
 */
public class AppendEntriesRequest implements Serializable {

    //  任期
    private long term;

    //  当前leader
    private String leaderId;

    //  日志索引号
    private long preLogIndex;

    //  日志保存的任期
    private long preLogTerm;

    //  日志列表
    private List<RaftLog> entries;

    //  最后提交日志索引号
    private long commitIndex;


    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public long getPreLogIndex() {
        return preLogIndex;
    }

    public void setPreLogIndex(long preLogIndex) {
        this.preLogIndex = preLogIndex;
    }

    public long getPreLogTerm() {
        return preLogTerm;
    }

    public void setPreLogTerm(long preLogTerm) {
        this.preLogTerm = preLogTerm;
    }

    public List<RaftLog> getEntries() {
        return entries;
    }

    public void setEntries(List<RaftLog> entries) {
        this.entries = entries;
    }

    public long getCommitIndex() {
        return commitIndex;
    }

    public void setCommitIndex(long commitIndex) {
        this.commitIndex = commitIndex;
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
    }
}
