package com.tongbanjie.raft.core.cmd;

import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

/***
 * raft command
 * @author banxia
 * @date 2017-11-29 15:15:26
 */
public class RaftCommand implements Serializable {


    //  命令名称 such as: raft:leave  raft:join
    private String name;
    /**
     * 类型
     *
     * @see com.tongbanjie.raft.core.enums.RaftCommandType
     */
    private Integer type;

    /**
     * 链接字符串 ip+port
     */
    private String connectStr;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getConnectStr() {
        return connectStr;
    }

    public void setConnectStr(String connectStr) {
        this.connectStr = connectStr;
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
    }
}
