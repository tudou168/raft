package com.tongbanjie.raft.core.transport.netty.codec;

import com.tongbanjie.raft.core.transport.Request;
import com.tongbanjie.raft.core.transport.Response;
import com.tongbanjie.raft.core.transport.enums.ChannelType;
import com.tongbanjie.raft.core.transport.netty.serialization.Serialization;
import com.tongbanjie.raft.core.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/***
 *
 * @author banxia
 * @date 2017-12-02 17:17:56
 */
public class NettyDecoder extends LengthFieldBasedFrameDecoder {

    private Serialization serialization;
    private ChannelType channelType;

    public NettyDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, Serialization serialization, ChannelType channelType) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
        this.serialization = serialization;
        this.channelType = channelType;
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {

        if (in.readableBytes() < 4) {
            return null;
        }

        byte[] header = new byte[4];

        in.readBytes(header);
        int length = ByteUtil.bytes2int(header, 0);
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return null;
        }

        byte[] body = new byte[length];
        in.readBytes(body);
        if (channelType.isClient()) {
            return this.serialization.deserialize(body, Response.class);
        } else {
            return this.serialization.deserialize(body, Request.class);
        }


    }
}
