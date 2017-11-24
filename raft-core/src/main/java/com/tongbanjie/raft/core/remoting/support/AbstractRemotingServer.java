package com.tongbanjie.raft.core.remoting.support;

import com.tongbanjie.raft.core.enums.RemotingChannelState;
import com.tongbanjie.raft.core.remoting.RemotingServer;

/***
 *
 * @author banxia
 * @date 2017-11-21 14:14:37
 */
public abstract class AbstractRemotingServer implements RemotingServer {

    protected RemotingChannelState state = RemotingChannelState.UNINIT;

}
