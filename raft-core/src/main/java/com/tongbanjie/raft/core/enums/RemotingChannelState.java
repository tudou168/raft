package com.tongbanjie.raft.core.enums;

/***
 *
 * @author banxia
 * @date 2017-11-20 21:21:19
 */
public enum RemotingChannelState {

    UNINIT(1),
    INIT(2),
    CLOSED(3);

    private final int value;

    RemotingChannelState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }


    public boolean isUninitState() {

        return this == UNINIT;
    }

    public boolean isInitState() {

        return this == INIT;
    }

    public boolean isClosedState() {

        return this == CLOSED;
    }
}
