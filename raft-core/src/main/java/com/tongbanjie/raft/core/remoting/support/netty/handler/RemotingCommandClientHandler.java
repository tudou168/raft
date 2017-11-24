package com.tongbanjie.raft.core.remoting.support.netty.handler;

import com.tongbanjie.raft.core.enums.RemotingCommandType;
import com.tongbanjie.raft.core.remoting.RemotingClientHandler;
import com.tongbanjie.raft.core.remoting.RemotingChannel;
import com.tongbanjie.raft.core.remoting.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 *
 * @author banxia
 * @date 2017-11-21 14:14:59
 */
public class RemotingCommandClientHandler extends SimpleChannelInboundHandler<RemotingCommand> {

    private final static Logger log = LoggerFactory.getLogger(RemotingCommandClientHandler.class);

    private RemotingChannel remotingChannel;
    private RemotingClientHandler remotingClientHandler;


    public RemotingCommandClientHandler(RemotingChannel remotingChannel, RemotingClientHandler remotingClientHandler) {

        this.remotingChannel = remotingChannel;
        this.remotingClientHandler = remotingClientHandler;
    }

    protected void channelRead0(ChannelHandlerContext ctx, RemotingCommand msg) throws Exception {

        if (msg == null) {
            throw new RuntimeException("response receive msg is null");
        }

        if (msg.getCommandType() != RemotingCommandType.HEARTBEAT.getValue()) {
            this.remotingClientHandler.handler(this.remotingChannel, msg);
        }


    }


}
