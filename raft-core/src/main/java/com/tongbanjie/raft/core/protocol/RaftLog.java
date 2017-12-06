package com.tongbanjie.raft.core.protocol;

import com.tongbanjie.raft.core.enums.RaftLogType;
import com.tongbanjie.raft.core.listener.LogApplyListener;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;

/***
 * raft log 实体
 * @author banxia
 * @date 2017-11-14 17:17:45
 */
public class RaftLog implements Serializable {


    //  序号
    private long index;

    //  任期
    private long term;

    // 日志
    private byte[] content;

    /**
     * type
     *
     * @see RaftLogType
     */
    private Integer type;


    // the log apply listener
    private LogApplyListener applyListener;


    public RaftLog(long index, long term, Integer type, byte[] content, LogApplyListener applyListener) {
        this.index = index;
        this.term = term;
        this.content = content;
        this.type = type;
        this.applyListener = applyListener;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public LogApplyListener getApplyListener() {
        return applyListener;
    }

    public void setApplyListener(LogApplyListener applyListener) {
        this.applyListener = applyListener;
    }

    public RaftLog createCopy() {

        return new RaftLog(this.index, this.term, this.type, this.content, null);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
    }
}
