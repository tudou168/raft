package com.tongbanjie.raft.core.remoting.netty.codec;

import com.tongbanjie.raft.core.remoting.RemotingCommand;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/***
 *  requestId(8) + state(4) + commandType(4) + body_length(4) + body
 * @author banxia
 * @date 2017-11-21 14:14:50
 */
public class RemotingCommandEncoder extends MessageToByteEncoder<RemotingCommand> {

    protected void encode(ChannelHandlerContext ctx, RemotingCommand msg, ByteBuf out) throws Exception {

        if (msg == null) {

            throw new RuntimeException("msg not allow null");
        }

        Long requestId = msg.getRequestId();
        Integer state = msg.getState();
        Integer commandType = msg.getCommandType();
        String body = msg.getBody();
        byte[] bytes = body.getBytes("UTF-8");

        out.writeLong(requestId);
        out.writeInt(state);
        out.writeInt(commandType);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}
