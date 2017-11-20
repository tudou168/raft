package com.tongbanjie.raft.core.protocol;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.List;

/***
 * 追加日志请求实体
 * @author banxia
 * @date 2017-11-19 11:11:16
 */
public class AppendEntriesRequest implements Serializable {

    private long term;
    private String leaderId;
    private long preLogIndex;
    private long preLogTerm;
    private List<RaftLog> entries;
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
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
