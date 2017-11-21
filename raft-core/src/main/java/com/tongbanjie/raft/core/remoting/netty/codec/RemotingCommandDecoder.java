package com.tongbanjie.raft.core.remoting.netty.codec;

import com.tongbanjie.raft.core.remoting.RemotingCommand;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 *  解码器
 * requestId(8) + state(4) + commandType(4) + body_length(4) + body
 * @author banxia
 * @date 2017-11-21 14:14:43
 */
public class RemotingCommandDecoder extends LengthFieldBasedFrameDecoder {


    private final static Logger log = LoggerFactory.getLogger(RemotingCommandDecoder.class);

    private final static int HEADER_LENGTH = 20;

    public RemotingCommandDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {

        log.info(">>>into RemotingCommandDecoder.decode...");
        if (in == null) {
            return null;
        }
        if (in.readableBytes() < HEADER_LENGTH) {

            return null;
        }

        long requestId = in.readLong();
        int state = in.readInt();
        int commandType = in.readInt();
        int length = in.readInt();

        byte[] bys = new byte[length];
        in.readBytes(bys);

        String body = new String(bys, "UTF-8");
        RemotingCommand command = new RemotingCommand();
        command.setRequestId(requestId);
        command.setState(state);
        command.setCommandType(commandType);
        command.setBody(body);
        log.info(">>>decode success command:" + command);
        return command;
    }
}
