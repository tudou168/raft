package com.tongbanjie.raft.core.transport.netty;

import com.sun.org.apache.bcel.internal.generic.NEW;
import com.tongbanjie.raft.core.exception.TransportException;
import com.tongbanjie.raft.core.exception.TransportTimeoutException;
import com.tongbanjie.raft.core.transport.Request;
import com.tongbanjie.raft.core.transport.Response;
import com.tongbanjie.raft.core.transport.ResponseFuture;
import com.tongbanjie.raft.core.transport.TransportCaller;
import com.tongbanjie.raft.core.transport.enums.TransportChannelState;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/***
 *
 * @author banxia
 * @date 2017-12-03 12:12:54
 */
public class NettyChannel implements TransportCaller {


    private final static Logger log = LoggerFactory.getLogger(NettyChannel.class);

    private TransportChannelState state = TransportChannelState.INIT;

    private Channel channel;

    private NettyClient nettyClient;

    private long requestTimeout;


    public NettyChannel(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }

    public long getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(long requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    @Override
    public boolean open() {
        if (isAvailable()) {
            return true;
        }

        try {
            ChannelFuture channelFuture = this.nettyClient.getBootstrap().connect(this.nettyClient.getHost(), this.nettyClient.getPort()).sync();
            if (channelFuture.isDone() && channelFuture.isSuccess()) {
                this.channel = channelFuture.channel();
                this.state = TransportChannelState.ALIVE;
                return true;
            }

        } catch (InterruptedException e) {
            log.error("the nettyChannel open fail", e);

        }
        throw new TransportException("the nettyChannel open fail");
    }

    @Override
    public void close() {

        if (isClosed()) {
            return;
        }

        if (this.channel != null) {

            try {
                this.channel.close();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        this.state = TransportChannelState.CLOSED;

    }

    @Override
    public boolean isClosed() {
        return this.state.isClosedState();
    }

    @Override
    public boolean isAvailable() {
        return this.state.isAliveState() && this.channel.isActive();
    }

    @Override
    public ResponseFuture request(Request request) {

        // create the response future

        NettyResponseFuture nettyResponseFuture = new NettyResponseFuture(request, this.nettyClient.getRequestTimeout());
        this.nettyClient.registerCallback(request.getRequestId(), nettyResponseFuture);
        ChannelFuture writeFuture = this.channel.writeAndFlush(request);

        boolean result = writeFuture.awaitUninterruptibly(this.nettyClient.getRequestTimeout(), TimeUnit.MILLISECONDS);
        if (result && writeFuture.isSuccess()) {

            return nettyResponseFuture;
        }


        writeFuture.cancel(true);

        NettyResponseFuture responseFuture = nettyClient.unregisterCallback(request.getRequestId());
        if (responseFuture != null) {
            responseFuture.cancel();
        }

        if (writeFuture.cause() != null) {

            throw new TransportException("NettyChannel send request to server Error");
        } else {
            throw new TransportTimeoutException("NettyChannel send request to server Timeout");
        }
    }
}
