package com.tongbanjie.raft.core.enums;

/***
 * raft 配置状态
 * @author banxia
 * @date 2017-11-15 16:16:55
 */
public enum RaftConfigurationState {


    COLD("Cold"), CNEW("Cold,Cnew");

    private String name;

    RaftConfigurationState(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }
}
