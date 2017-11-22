package com.tongbanjie.raft.core.enums;

/***
 *  命令类型
 *
 * @author banxia
 * @date 2017-11-20 21:21:06
 */
public enum RemotingCommandType {

    // 心跳
    HEARTBEAT(0),
    //  选举
    ELECTION(1),
    // 追加日志
    APPEND(2);

    private final int value;

    RemotingCommandType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
