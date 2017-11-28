package com.tongbanjie.raft.core.remoting.support.netty;

import com.tongbanjie.raft.core.enums.RemotingChannelState;
import com.tongbanjie.raft.core.enums.RemotingCommandState;
import com.tongbanjie.raft.core.remoting.*;
import com.tongbanjie.raft.core.remoting.support.AbstractRemotingPooledClient;
import com.tongbanjie.raft.core.remoting.support.netty.codec.RemotingCommandDecoder;
import com.tongbanjie.raft.core.remoting.support.netty.codec.RemotingCommandEncoder;
import com.tongbanjie.raft.core.remoting.future.support.NettyResponseFuture;
import com.tongbanjie.raft.core.remoting.support.netty.handler.heartbeat.HeartbeatClientHandler;
import com.tongbanjie.raft.core.remoting.support.netty.handler.RemotingCommandClientHandler;
import com.tongbanjie.raft.core.remoting.support.AbstractRemotingClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.commons.pool2.PooledObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/***
 *
 * @author banxia
 * @date 2017-11-21 15:15:10
 */
public class NettyClient extends AbstractRemotingPooledClient {

    private final static Logger log = LoggerFactory.getLogger(NettyClient.class);

    private final static int MAX_FRAME_LENGTH = 1024 * 1024;

    private final static int LENGTH_FIELD_OFF_SET = 16;

    private final static int LENGTH_FIELD_LENGTH = 4;

    private int requestTimeout;

    private String host;

    private int port;


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private Bootstrap bootstrap;

    private EventLoopGroup workerGroup;

    public int getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    private ConcurrentHashMap<Long, NettyResponseFuture> futureMap = new ConcurrentHashMap<Long, NettyResponseFuture>();

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public void registerCallback(NettyResponseFuture nettyResponseFuture) {

        this.futureMap.put(nettyResponseFuture.getRequestId(), nettyResponseFuture);

    }


    public NettyResponseFuture removeNettyResponseFuture(Long requestId) {

        return this.futureMap.remove(requestId);

    }


    public synchronized boolean open() {


        if (this.state.isAliveState()) {
            log.warn(String.format("the netty client [%s:%s] is already open...", host, port));
            return true;
        }
        // 初始化
        this.initBootstrap();
        this.initPool();

        log.info(String.format("the netty client [%s:%s]    open success...", host, port));
        this.state = RemotingChannelState.ALIVE;
        return true;


    }


    /**
     * 初始化
     */
    private void initBootstrap() {

        this.bootstrap = new Bootstrap();
        this.workerGroup = new NioEventLoopGroup();
        this.bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(final SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new RemotingCommandEncoder());
                        ch.pipeline().addLast(new RemotingCommandDecoder(MAX_FRAME_LENGTH, LENGTH_FIELD_OFF_SET, LENGTH_FIELD_LENGTH));
                        ch.pipeline().addLast(new IdleStateHandler(0, 0, 5));
                        ch.pipeline().addLast(new RemotingCommandClientHandler(NettyClient.this, new RemotingClientHandler() {
                            public void handler(RemotingChannel channel, Object msg) {
                                RemotingCommand command = (RemotingCommand) msg;
                                NettyResponseFuture responseFuture = NettyClient.this.removeNettyResponseFuture(command.getRequestId());
                                if (responseFuture == null) {
                                    return;
                                }
                                if (command.getState() == RemotingCommandState.SUCCESS.getValue()) {
                                    responseFuture.onSuccess(command);
                                } else if (command.getState() == RemotingCommandState.FAIL.getValue()) {
                                    responseFuture.onFail(command);
                                }

                            }
                        }));
                        ch.pipeline().addLast(new HeartbeatClientHandler());
                    }
                });


    }

    /**
     * 关闭通道
     */
    public synchronized void close() {

        if (this.state.isUninitState()) {
            log.warn("the netty client has no init...");
            return;
        }

        if (this.state.isClosedState()) {
            log.warn("the netty client has already close...");
            return;
        }


        try {

            this.workerGroup.shutdownGracefully();
            this.state = RemotingChannelState.CLOSED;
        } catch (Exception e) {
            log.error("the netty  close fail", e);
            throw new RuntimeException("the netty  close fail", e);
        }


    }

    public boolean isClosed() {

        return this.state.isClosedState();
    }

    public boolean isAvailable() {
        return this.state.isAliveState();
    }

    public RemotingCommand request(RemotingCommand command) {


        NettyChannel nettyChannel = null;
        RemotingCommand response = null;
        try {

            nettyChannel = this.borrowChannel();
            response = nettyChannel.request(command);
            this.returnChannel(nettyChannel);
        } catch (Exception e) {
            log.error("nettyClient request fail", e);
            this.invalidateChannel(nettyChannel);
            throw new RuntimeException("nettyClient request fail", e);

        }

        return response;

    }


    protected PooledObjectFactory newNettyChannelPooledFactory() {
        return new NettyPooledChannelFactory(this);
    }
}
