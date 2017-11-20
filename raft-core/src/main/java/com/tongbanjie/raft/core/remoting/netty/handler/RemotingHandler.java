package com.tongbanjie.raft.core.remoting.netty.handler;

import com.tongbanjie.raft.core.remoting.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/***
 *
 * @author banxia
 * @date 2017-11-20 21:21:30
 */
public class RemotingHandler extends SimpleChannelInboundHandler<RemotingCommand> {
    protected void channelRead0(ChannelHandlerContext ctx, RemotingCommand msg) throws Exception {

    }
}
