package com.tongbanjie.raft.core.transport.netty;

import com.sun.javaws.exceptions.InvalidArgumentException;
import com.tongbanjie.raft.core.exception.TransportException;
import com.tongbanjie.raft.core.transport.AbstractTransportServer;
import com.tongbanjie.raft.core.transport.Request;
import com.tongbanjie.raft.core.transport.Response;
import com.tongbanjie.raft.core.transport.enums.ChannelType;
import com.tongbanjie.raft.core.transport.enums.TransportChannelState;
import com.tongbanjie.raft.core.transport.netty.codec.NettyDecoder;
import com.tongbanjie.raft.core.transport.netty.codec.NettyEncoder;
import com.tongbanjie.raft.core.transport.netty.dispatcher.RequestHandlerDispatcher;
import com.tongbanjie.raft.core.transport.netty.handler.NettyServerHandler;
import com.tongbanjie.raft.core.transport.netty.serialization.Serialization;
import com.tongbanjie.raft.core.util.NetUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/***
 *
 * @author banxia
 * @date 2017-12-02 16:16:42
 */
public class NettyServer<T> extends AbstractTransportServer {

    private final static Logger log = LoggerFactory.getLogger(NettyServer.class);

    private String host;

    private int port;

    private T ref;


    private ServerBootstrap serverBootstrap;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private Serialization serialization;

    private Channel channel;

    private int threadNum;


    public Serialization getSerialization() {
        return serialization;
    }

    public void setSerialization(Serialization serialization) {
        this.serialization = serialization;
    }

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

    public T getRef() {
        return ref;
    }

    public void setRef(T ref) {
        this.ref = ref;
    }

    public int getThreadNum() {
        if (threadNum == 0) {
            threadNum = Runtime.getRuntime().availableProcessors();
        }
        return threadNum;
    }

    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }

    @Override
    public boolean open() {

        if (this.state.isAliveState()) {
            log.warn(String.format("the nettyServer %s:%s is already open!", this.host, this.port));
            return true;
        }

        this.checkParam();

        this.serverBootstrap = new ServerBootstrap();
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();

        this.serverBootstrap.group(bossGroup, workerGroup)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast("encoder", new NettyEncoder(serialization));
                ch.pipeline().addLast("decoder", new NettyDecoder(1024 * 1024 * 10, 0, 4, serialization, ChannelType.SERVER));
                ch.pipeline().addLast("handler", new NettyServerHandler<T>(new RequestHandlerDispatcher<T>(ref, threadNum)));
            }
        });

        try {
            ChannelFuture channelFuture = this.serverBootstrap.bind(this.host, this.port).sync();
            this.channel = channelFuture.channel();
            this.state = TransportChannelState.ALIVE;
            log.info(String.format("the nettyServer %s:%s is  open succes!", this.host, this.port));
        } catch (InterruptedException e) {
            log.error(String.format("the nettyServer %s:%s is  open fail!", this.host, this.port), e);
            throw new TransportException(String.format("the nettyServer %s:%s is  open fail!", this.host, this.port), e);

        }
        return true;
    }

    private void checkParam() {

        if (StringUtils.isBlank(this.host)) {
            this.host = NetUtil.getLocalAddress().getHostAddress();
        }
        if (this.port == 0) {
            throw new TransportException("port not allow exception");
        }

        if (null == this.ref) {
            throw new TransportException("reft not allow null!");
        }


    }

    @Override
    public void close() {

        if (this.state.isClosedState()) {
            log.info(String.format("the nettyServer %s:%s is  already close!", this.host, this.port));
            return;
        }
        if (this.state.isUinitState()) {
            log.info(String.format("the nettyServer %s:%s is has not open!", this.host, this.port));
            return;
        }


        this.workerGroup.shutdownGracefully();
        this.bossGroup.shutdownGracefully();
        this.state = TransportChannelState.CLOSED;

        log.info(String.format("the nettyServer %s:%s is  close success!", this.host, this.port));

    }

    @Override
    public boolean isClosed() {
        return this.state.isClosedState();
    }

    @Override
    public boolean isAvailable() {
        return this.state.isAliveState() && this.channel.isActive();
    }
}
