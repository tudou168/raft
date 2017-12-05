package com.tongbanjie.raft.core.enums;

/***
 * raft  log type enum
 * @author banxia
 * @date 2017-11-24 14:14:26
 */
public enum RaftLogType {

    //  数据
    DATA(1),
    // 配置
    CONFIGURATION(2),
    CONFIGURATION_NEW(3);

    private final int value;

    RaftLogType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
