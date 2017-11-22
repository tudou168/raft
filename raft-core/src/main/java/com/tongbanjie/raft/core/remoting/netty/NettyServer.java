package com.tongbanjie.raft.core.remoting.netty;

import com.tongbanjie.raft.core.enums.RemotingChannelState;
import com.tongbanjie.raft.core.remoting.AbstractRemotingServer;
import com.tongbanjie.raft.core.remoting.RemotingCommand;
import com.tongbanjie.raft.core.remoting.netty.codec.RemotingCommandDecoder;
import com.tongbanjie.raft.core.remoting.netty.codec.RemotingCommandEncoder;
import com.tongbanjie.raft.core.remoting.netty.handler.heartbeat.HeartbeatServerHandler;
import com.tongbanjie.raft.core.remoting.netty.handler.RemotingCommandServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 *  基于 netty 实现服务端
 * @author banxia
 * @date 2017-11-21 14:14:36
 */
public class NettyServer extends AbstractRemotingServer {


    private final static Logger log = LoggerFactory.getLogger(NettyServer.class);

    private final static int MAX_FRAME_LENGTH = 1024 * 1024;

    private final static int LENGTH_FIELD_OFF_SET = 16;

    private final static int LENGTH_FIELD_LENGTH = 4;


    private ServerBootstrap serverBootstrap;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private ChannelFuture channelFuture;

    private RemotingCommandProcessor commandProcessor;


    public NettyServer(RemotingCommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
    }

    public ChannelFuture getChannelFuture() {
        return channelFuture;
    }

    public boolean open(String host, int port) {


        if (this.state.isInitState()) {
            log.warn(String.format("the netty server [%s:%s] is already open...", host, port));
            return true;
        }
        this.initBootstrap();
        log.info(String.format("the netty server [%s:%s]  start  open...", host, port));
        try {

            this.channelFuture = this.serverBootstrap.bind(host, port).sync();
            this.state = RemotingChannelState.INIT;
            log.info(String.format("the netty server [%s:%s]  open success...", host, port));
            return true;

        } catch (InterruptedException e) {
            log.error(String.format("the netty server [%s:%s] is  open fail...", host, port), e);
        }

        return false;
    }


    /**
     * 初始化
     */
    private void initBootstrap() {

        this.serverBootstrap = new ServerBootstrap();
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
        this.serverBootstrap.group(bossGroup, workerGroup);
        this.serverBootstrap.channel(NioServerSocketChannel.class);
        this.serverBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        this.serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        this.serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel ch) throws Exception {

                ch.pipeline().addLast(new RemotingCommandEncoder());
                ch.pipeline().addLast(new RemotingCommandDecoder(MAX_FRAME_LENGTH, LENGTH_FIELD_OFF_SET, LENGTH_FIELD_LENGTH));
                ch.pipeline().addLast(new IdleStateHandler(15, 0, 0));
                ch.pipeline().addLast(new RemotingCommandServerHandler(commandProcessor));
                ch.pipeline().addLast(new HeartbeatServerHandler());

            }
        });


    }

    public void close() {

        if (this.state.isClosedState()) {
            log.warn("the netty server already close...");
            return;
        }

        if (this.state.isUninitState()) {
            log.warn("the netty server has not init...");
            return;
        }

        if (this.channelFuture != null) {
            try {

                this.channelFuture.channel().closeFuture().sync();
                this.bossGroup.shutdownGracefully();
                this.workerGroup.shutdownGracefully();
                this.state = RemotingChannelState.CLOSED;

            } catch (InterruptedException e) {
                log.error("the netty server close fail", e);
            }
        }
    }

    /**
     * 是否已经关闭
     *
     * @return
     */
    public boolean isClosed() {

        return this.state.isClosedState();
    }

    /**
     * @param command
     * @return
     */
    public RemotingCommand request(RemotingCommand command) {

        throw new RuntimeException(" not support request ...");
    }


    public void doConnect() {

        throw new UnsupportedOperationException("not support doConnect ");
    }

}
