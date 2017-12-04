package com.tongbanjie.raft.core.transport.netty.handler;

import com.tongbanjie.raft.core.transport.Request;
import com.tongbanjie.raft.core.transport.netty.RequestWrapper;
import com.tongbanjie.raft.core.transport.netty.dispatcher.RequestHandlerDispatcher;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;

/***
 *
 * @author banxia
 * @date 2017-12-02 17:17:58
 */
public class NettyServerHandler<T> extends SimpleChannelInboundHandler<Request> {


    private RequestHandlerDispatcher<T> dispatcher;

    public NettyServerHandler(RequestHandlerDispatcher<T> dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request msg) throws Exception {

        if (null != msg) {

            RequestWrapper wrapper = new RequestWrapper(msg, ctx.channel());
            this.dispatcher.addRequestWrapper(wrapper);

        }

    }
}
