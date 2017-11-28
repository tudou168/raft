package com.tongbanjie.raft.core.remoting.support.netty.handler.heartbeat;

import com.tongbanjie.raft.core.enums.RemotingCommandState;
import com.tongbanjie.raft.core.enums.RemotingCommandType;
import com.tongbanjie.raft.core.remoting.RemotingChannel;
import com.tongbanjie.raft.core.remoting.RemotingCommand;
import com.tongbanjie.raft.core.util.RequestIdGenerator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 *
 * @author banxia
 * @date 2017-11-22 09:09:31
 */
public class HeartbeatClientHandler extends SimpleChannelInboundHandler<RemotingCommand> {

    private final static Logger log = LoggerFactory.getLogger(HeartbeatClientHandler.class);

    private long heartbeatCount;


    public HeartbeatClientHandler() {

    }

    protected void channelRead0(ChannelHandlerContext ctx, RemotingCommand msg) throws Exception {

        // 向服务发送 ping 消息
        if (RemotingCommandType.HEARTBEAT.getValue() == msg.getCommandType()) {
            log.info(String.format("get pong msg from %s", ctx.channel().remoteAddress()));
        }
    }


    /**
     * 向服务端发送 ping 消息
     *
     * @param ctx
     */
    private void sendPingMsg(ChannelHandlerContext ctx) {


        RemotingCommand ping = new RemotingCommand();
        ping.setRequestId(RequestIdGenerator.getRequestId());
        ping.setState(RemotingCommandState.SUCCESS.getValue());
        ping.setCommandType(RemotingCommandType.HEARTBEAT.getValue());
        ping.setBody("ping");
        ctx.writeAndFlush(ping);
        this.heartbeatCount++;
        log.info(String.format(" sent ping msg to %s,heartbeatCount:%s", ctx.channel().remoteAddress(), this.heartbeatCount));
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {

            if (((IdleStateEvent) evt).state() == IdleState.ALL_IDLE) {
                // 向服务端发送 ping消息
                sendPingMsg(ctx);
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

    }
}
