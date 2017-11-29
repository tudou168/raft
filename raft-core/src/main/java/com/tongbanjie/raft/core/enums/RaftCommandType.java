package com.tongbanjie.raft.core.enums;

/***
 * raft command type
 * @author banxia
 * @date 2017-11-29 15:15:30
 */
public enum RaftCommandType {

    JOIN(1), LEAVE(2);

    private final int value;

    RaftCommandType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
