package com.tongbanjie.raft.core.enums;

/***
 *
 * @author banxia
 * @date 2017-11-21 10:10:39
 */
public enum FutureState {

    DOING(0), DONE(1), CANCELED(3);

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
