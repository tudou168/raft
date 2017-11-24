package com.tongbanjie.raft.core.remoting.support.netty.codec;

import com.tongbanjie.raft.core.remoting.RemotingCommand;
import com.tongbanjie.raft.core.util.ByteUtil;
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


        byte[] header = new byte[20];
        int offset = 0;
        ByteUtil.long2bytes(requestId, header, offset);
        offset += 8;

        ByteUtil.int2bytes(state, header, offset);
        offset += 4;

        ByteUtil.int2bytes(commandType, header, offset);
        offset += 4;


        byte[] bytes = body.getBytes("UTF-8");

        int length = bytes.length;

        ByteUtil.int2bytes(length, header, offset);

        byte[] content = new byte[header.length + bytes.length];
        System.arraycopy(header, 0, content, 0, header.length);
        System.arraycopy(bytes, 0, content, header.length, bytes.length);
        out.writeBytes(content);

    }
}
