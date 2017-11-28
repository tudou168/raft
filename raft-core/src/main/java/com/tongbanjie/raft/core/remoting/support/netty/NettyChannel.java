package com.tongbanjie.raft.core.remoting.support.netty;

import com.tongbanjie.raft.core.enums.RemotingChannelState;
import com.tongbanjie.raft.core.remoting.RemotingChannel;
import com.tongbanjie.raft.core.remoting.RemotingCommand;
import com.tongbanjie.raft.core.remoting.future.support.NettyResponseFuture;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 *
 * @author banxia
 * @date 2017-11-28 09:09:51
 */
public class NettyChannel implements RemotingChannel {

    private final static Logger log = LoggerFactory.getLogger(NettyChannel.class);
    private RemotingChannelState state = RemotingChannelState.UNINIT;
    private Channel channel;
    private NettyClient nettyClient;

    public NettyChannel(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }

    public boolean open() {

        if (this.state.isAliveState()) {
            return true;
        }

        try {
            ChannelFuture channelFuture = this.nettyClient.getBootstrap().connect(this.nettyClient.getHost(), this.nettyClient.getPort()).sync();
            if (channelFuture.isSuccess()) {
                this.channel = channelFuture.channel();
                this.state = RemotingChannelState.ALIVE;
            }

            return true;
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("open netty channel fail", e);
        }
    }

    public void close() {

        try {
            state = RemotingChannelState.CLOSED;
            if (this.channel != null) {
                this.channel.close();
            }
        } catch (Exception e) {
            log.error("the netty channel close fail", e);
        }
    }

    public boolean isClosed() {
        return state.isClosedState();
    }

    public boolean isAvailable() {
        return state.isAliveState() && this.channel.isActive();
    }

    public RemotingCommand request(RemotingCommand command) {


        NettyResponseFuture responseFuture = new NettyResponseFuture(command, this.nettyClient.getRequestTimeout());
        this.nettyClient.registerCallback(responseFuture);
        ChannelFuture writeFuture = this.channel.writeAndFlush(command);
        boolean result = writeFuture.awaitUninterruptibly(this.nettyClient.getRequestTimeout());
        if (result && writeFuture.isSuccess()) {

            return responseFuture.getRemotingCommand();
        }

        // fail
        writeFuture.cancel(false);
        NettyResponseFuture response = this.nettyClient.removeNettyResponseFuture(command.getRequestId());

        if (response != null) {
            response.cancel();
        }
        throw new RuntimeException("request fail  request :" + command);
    }
}
