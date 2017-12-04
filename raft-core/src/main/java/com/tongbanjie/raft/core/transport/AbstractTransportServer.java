package com.tongbanjie.raft.core.transport;

import com.tongbanjie.raft.core.transport.enums.TransportChannelState;

/***
 * abstract  transport server
 * @author banxia
 * @date 2017-12-02 16:16:26
 */
public abstract class AbstractTransportServer implements TransportServer {

    protected TransportChannelState state = TransportChannelState.UINIT;


}
