package com.tongbanjie.raft.core.transport;

import com.tongbanjie.raft.core.transport.enums.TransportChannelState;

/***
 *
 * @author banxia
 * @date 2017-12-02 23:23:19
 */
public abstract class AbstractTransportClient implements TransportClient {

    protected TransportChannelState state = TransportChannelState.UINIT;


}
