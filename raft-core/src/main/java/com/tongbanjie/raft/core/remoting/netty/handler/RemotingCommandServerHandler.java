package com.tongbanjie.raft.core.remoting.netty.handler;

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
public class RemotingCommandServerHandler extends SimpleChannelInboundHandler<RemotingCommand> {

    private final static Logger log = LoggerFactory.getLogger(RemotingCommandServerHandler.class);

    protected void channelRead0(ChannelHandlerContext ctx, RemotingCommand msg) throws Exception {


        if (msg == null) {
            throw new RuntimeException("receive msg is null");
        }

        log.info(">>>>>>>>>receive msg from client :" + msg);
        ctx.writeAndFlush(msg);

    }
}
