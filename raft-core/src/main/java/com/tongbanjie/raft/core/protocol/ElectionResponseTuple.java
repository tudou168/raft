package com.tongbanjie.raft.core.protocol;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/***
 *
 * @author banxia
 * @date 2017-11-16 21:21:19
 */
public class ElectionResponseTuple implements Serializable {

    private ElectionResponse electionResponse;

    private String id;

    private boolean success;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ElectionResponse getElectionResponse() {
        return electionResponse;
    }

    public void setElectionResponse(ElectionResponse electionResponse) {
        this.electionResponse = electionResponse;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
