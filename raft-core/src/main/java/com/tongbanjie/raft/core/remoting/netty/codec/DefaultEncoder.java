package com.tongbanjie.raft.core.remoting.netty.codec;

import com.tongbanjie.raft.core.constant.RaftConstant;
import com.tongbanjie.raft.core.remoting.RemotingCommand;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/***
 *  解码器
 *  protocol(version(4) + type(4) + commandType(4) + requestId(8) + body_length(4) + body )
 * @author banxia
 * @date 2017-11-20 20:20:55
 */
public class DefaultEncoder extends MessageToByteEncoder<RemotingCommand> {
    protected void encode(ChannelHandlerContext ctx, RemotingCommand msg, ByteBuf out) throws Exception {


        if (msg == null) {
            throw new Exception("msg not allow null");
        }

        String body = msg.getBody();
        byte[] bytes = body.getBytes();
        ctx.write(RaftConstant.version);
        ctx.write(msg.getType());
        ctx.write(msg.getCommandType());
        ctx.write(msg.getRequestId());
        ctx.write(bytes.length);
        ctx.write(bytes);
    }
}
