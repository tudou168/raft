package com.tongbanjie.raft.core.remoting.netty.handler.heartbeat;

import com.tongbanjie.raft.core.enums.RemotingCommandState;
import com.tongbanjie.raft.core.enums.RemotingCommandType;
import com.tongbanjie.raft.core.remoting.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.timeout.IdleState.READER_IDLE;

/***
 *  心跳服务端
 * @author banxia
 * @date 2017-11-22 09:09:31
 */
public class HeartbeatServerHandler extends SimpleChannelInboundHandler<RemotingCommand> {

    private final static Logger log = LoggerFactory.getLogger(HeartbeatServerHandler.class);

    private long heartbeatCount = 0;

    protected void channelRead0(ChannelHandlerContext ctx, RemotingCommand msg) throws Exception {

        // 向客户端 pong
        if (RemotingCommandType.HEARTBEAT.getValue() == msg.getCommandType()) {
            log.info(String.format("get ping msg from %s", ctx.channel().remoteAddress()));
            RemotingCommand pong = new RemotingCommand();
            pong.setRequestId(msg.getRequestId());
            pong.setState(RemotingCommandState.SUCCESS.getValue());
            pong.setBody("pong");
            ctx.writeAndFlush(pong);
            this.heartbeatCount++;
            log.info(String.format(" sent pong msg to %s,heartbeatCount:%s", ctx.channel().remoteAddress(), this.heartbeatCount));
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {

            if (((IdleStateEvent) evt).state() == READER_IDLE) {

                log.warn(String.format("---client  %s reader timeout, close it---", ctx.channel().remoteAddress().toString()));
                ctx.close();
            }

        }
    }
}
