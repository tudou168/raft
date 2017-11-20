package com.tongbanjie.raft.core.remoting.netty.codec;

import com.tongbanjie.raft.core.constant.RaftConstant;
import com.tongbanjie.raft.core.remoting.RemotingCommand;
import com.tongbanjie.raft.core.remoting.Request;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/***
 *  解码器
 *  protocol(version(4) + type(4) + commandType(4) + requestId(8) + body_length(4) + body )
 * @author banxia
 * @date 2017-11-20 20:20:38
 */
public class DefaultDecoder extends LengthFieldBasedFrameDecoder {


    private static final int headerLength = 24;

    public DefaultDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
    }


    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {

        //  判断消息头是否足够长
        if (null == in || in.readableBytes() < headerLength) {
            return null;
        }

        int version = in.readInt();
        if (version != RaftConstant.version) {
            throw new Exception("not support version:" + version);
        }
        int type = in.readInt();
        int commandType = in.readInt();
        long requestId = in.readLong();
        int length = in.readInt();

        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return null;
        }

        byte[] buf = new byte[length];
        in.readBytes(buf);

        String body = new String(buf, "UTF-8");
        RemotingCommand command = new RemotingCommand();
        command.setBody(body);
        command.setRequestId(requestId);
        command.setType(type);
        command.setCommandType(commandType);
        return command;


    }
}
