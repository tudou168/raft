package com.tongbanjie.raft.core.remoting;

import com.tongbanjie.raft.core.enums.RemotingChannelState;

/***
 *
 * @author banxia
 * @date 2017-11-20 21:21:22
 */
public abstract class AbstractRemotingServer implements RemotingServer {


    protected RemotingChannelState state = RemotingChannelState.UNINIT;

}
