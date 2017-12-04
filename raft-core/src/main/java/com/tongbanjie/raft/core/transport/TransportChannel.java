package com.tongbanjie.raft.core.transport;

import java.lang.reflect.Method;

/***
 *
 * @author banxia
 * @date 2017-12-02 15:15:56
 */
public interface TransportChannel {


    //  open the channel
    boolean open();

    //  close the channel
    void close();

    //  check the channel is closed
    boolean isClosed();

    //  check the channel is available
    boolean isAvailable();

}
