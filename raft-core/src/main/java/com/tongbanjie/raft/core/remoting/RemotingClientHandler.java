package com.tongbanjie.raft.core.remoting;

/***
 *
 * @author banxia
 * @date 2017-11-21 17:17:59
 */
public interface RemotingClientHandler {


    void handler(RemotingChannel channel, Object msg);
}
