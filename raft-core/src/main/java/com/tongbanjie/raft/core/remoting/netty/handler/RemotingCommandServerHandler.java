package com.tongbanjie.raft.core.remoting.netty.handler;

import com.alibaba.fastjson.JSON;
import com.tongbanjie.raft.core.enums.RemotingCommandType;
import com.tongbanjie.raft.core.protocol.ElectionRequest;
import com.tongbanjie.raft.core.remoting.netty.RemotingCommandProcessor;
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

    private RemotingCommandProcessor commandProcessor;

    public RemotingCommandServerHandler(RemotingCommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
    }

    protected void channelRead0(ChannelHandlerContext ctx, RemotingCommand msg) throws Exception {


        if (msg == null) {
            throw new RuntimeException("receive msg is null");
        }

        log.info(">>>>>>>>>receive msg from client :" + msg);

        if (msg.getCommandType() == RemotingCommandType.HEARTBEAT.getValue()) {
            ctx.fireChannelRead(msg);

        } else if (msg.getCommandType() == RemotingCommandType.ELECTION.getValue()) {

            this.commandProcessor.electionVoteHandler(ctx, msg);

        } else if (msg.getCommandType() == RemotingCommandType.APPEND.getValue()) {

            this.commandProcessor.appendEntriesHandler(ctx, msg);

        }




    }
}
