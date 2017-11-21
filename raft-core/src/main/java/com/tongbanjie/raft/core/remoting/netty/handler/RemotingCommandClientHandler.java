package com.tongbanjie.raft.core.remoting.netty.handler;

import com.tongbanjie.raft.core.remoting.MessageHandler;
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
    private MessageHandler messageHandler;


    public RemotingCommandClientHandler(RemotingChannel remotingChannel, MessageHandler messageHandler) {
        this.remotingChannel = remotingChannel;
        this.messageHandler = messageHandler;
    }

    protected void channelRead0(ChannelHandlerContext ctx, RemotingCommand msg) throws Exception {

        if (msg == null) {
            throw new RuntimeException("response receive msg is null");
        }

        this.messageHandler.handler(this.remotingChannel, msg);
        log.info(">>>>>> receive msg from server :" + msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        System.err.println(">>>>>>>>>>>>channelInactive<<<<<<<<<<<<");
        this.remotingChannel.doConnect();
    }
}
