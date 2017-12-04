package com.tongbanjie.raft.core.transport.netty.codec;

import com.tongbanjie.raft.core.transport.Request;
import com.tongbanjie.raft.core.transport.enums.ChannelType;
import com.tongbanjie.raft.core.transport.netty.serialization.Serialization;
import com.tongbanjie.raft.core.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/***
 *
 * @author banxia
 * @date 2017-12-02 17:17:56
 */
public class NettyEncoder extends MessageToByteEncoder<Object> {

    private Serialization serialization;

    public NettyEncoder(Serialization serialization) {
        this.serialization = serialization;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {

        byte[] bytes = this.serialization.serialize(msg);
        int length = bytes.length;

        byte[] header = new byte[4];
        ByteUtil.int2bytes(length, header, 0);
        byte[] body = new byte[header.length + bytes.length];
        System.arraycopy(header, 0, body, 0, header.length);
        System.arraycopy(bytes, 0, body, header.length, bytes.length);
        out.writeBytes(body);
    }
}
