package com.tongbanjie.raft.core.election;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/***
 * 选举投票响应实体
 * @author banxia
 * @date 2017-11-15 19:19:55
 */
public class ElectionVoteResponse implements Serializable {

    //  当前任期号，以便于候选人去更新自己的任期号
    private long term;

    //  候选人赢得了此张选票时为真
    private boolean voteGranted;

    //  失败原因
    private String reason;


    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public boolean isVoteGranted() {
        return voteGranted;
    }

    public void setVoteGranted(boolean voteGranted) {
        this.voteGranted = voteGranted;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
