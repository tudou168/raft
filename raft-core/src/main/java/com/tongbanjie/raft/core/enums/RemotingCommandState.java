package com.tongbanjie.raft.core.enums;

/***
 *  远程执行状态
 * @author banxia
 * @date 2017-11-21 09:09:15
 */
public enum RemotingCommandState {

    SUCCESS(1), FAIL(2);

    private final int value;

    RemotingCommandState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
