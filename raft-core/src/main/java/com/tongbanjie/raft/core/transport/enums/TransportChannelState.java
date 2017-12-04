package com.tongbanjie.raft.core.transport.enums;

/***
 * transport state
 * @author banxia
 * @date 2017-12-02 16:16:27
 */
public enum TransportChannelState {

    UINIT(1), INIT(2), ALIVE(3), CLOSED(4);

    private final int value;

    TransportChannelState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public boolean isUinitState() {
        return this == UINIT;
    }

    public boolean isInitState() {
        return this == INIT;
    }

    public boolean isAliveState() {
        return this == ALIVE;
    }

    public boolean isClosedState() {
        return this == CLOSED;
    }


}
