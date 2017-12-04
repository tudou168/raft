package com.tongbanjie.raft.core.transport.enums;

/***
 *
 * @author banxia
 * @date 2017-12-04 11:11:29
 */
public enum FutureState {

    DOING(1), DONE(2), CANCELED(3);

    private final int value;

    FutureState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }


    public boolean isDoingState() {
        return this == DOING;
    }

    public boolean isDoneState() {
        return this == DONE;
    }

    public boolean isCanceledState() {
        return this == CANCELED;
    }

}
