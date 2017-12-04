package com.tongbanjie.raft.core.transport.enums;

/***
 *
 * @author banxia
 * @date 2017-12-02 19:19:02
 */
public enum ChannelType {
    CLIENT(1), SERVER(2);


    private final int value;

    ChannelType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }


    public boolean isClient() {
        return this == CLIENT;
    }

    public boolean isServer() {
        return this == SERVER;
    }
}
