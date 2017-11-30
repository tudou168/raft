package com.tongbanjie.raft.core.protocol;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;

/***
 *
 * @author banxia
 * @date 2017-11-19 11:11:15
 */
public class ReplicationLogResponseTuple implements Serializable {


    //  是否成功
    private boolean success;

    private AppendEntriesResponse appendEntriesResponse;


    private String reason;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public AppendEntriesResponse getAppendEntriesResponse() {
        return appendEntriesResponse;
    }

    public void setAppendEntriesResponse(AppendEntriesResponse appendEntriesResponse) {
        this.appendEntriesResponse = appendEntriesResponse;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
    }
}
