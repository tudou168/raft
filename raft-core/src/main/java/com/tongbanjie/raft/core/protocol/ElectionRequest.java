package com.tongbanjie.raft.core.protocol;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/***
 * 选举投票请求实体
 * @author banxia
 * @date 2017-11-15 19:19:46
 */
public class ElectionRequest implements Serializable {


    //  当前任期
    private String term;
    //  候选人 id
    private String candidateId;

    // 候选人的最后日志条目的索引值
    private long lastLogIndex;

    // 候选人最后日志条目的任期号
    private long lastLogTerm;


    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }

    public long getLastLogIndex() {
        return lastLogIndex;
    }

    public void setLastLogIndex(long lastLogIndex) {
        this.lastLogIndex = lastLogIndex;
    }

    public long getLastLogTerm() {
        return lastLogTerm;
    }

    public void setLastLogTerm(long lastLogTerm) {
        this.lastLogTerm = lastLogTerm;
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
