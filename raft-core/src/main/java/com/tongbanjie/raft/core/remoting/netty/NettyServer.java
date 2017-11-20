package com.tongbanjie.raft.core.remoting.netty;

import com.tongbanjie.raft.core.enums.RemotingChannelState;
import com.tongbanjie.raft.core.remoting.AbstractRemotingServer;
import com.tongbanjie.raft.core.remoting.RemotingChannel;
import com.tongbanjie.raft.core.remoting.RemotingCommand;
import com.tongbanjie.raft.core.remoting.netty.codec.DefaultDecoder;
import com.tongbanjie.raft.core.remoting.netty.codec.DefaultEncoder;
import com.tongbanjie.raft.core.remoting.netty.handler.RemotingHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/***
 *
 * @author banxia
 * @date 2017-11-20 21:21:14
 */
public class NettyServer extends AbstractRemotingServer {

    private final static Logger log = LoggerFactory.getLogger(NettyServer.class);

    private ServerBootstrap serverBootstrap;

    private RemotingHandler remotingHandler;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workGroup;
    private ChannelFuture channelFuture;

    private DefaultEncoder encoder = new DefaultEncoder();
    private DefaultDecoder decoder = new DefaultDecoder(1024, 0, 100);


    public boolean open(String host, int port) {

        if (this.state.isInitState()) {

            log.warn("the netty server has already init");
            return true;
        }

        log.info("the netty server start open...");
        this.initBootstrap();

        try {
            channelFuture = this.serverBootstrap.bind(host, port).sync();
            this.state = RemotingChannelState.INIT;
            log.info("the netty server start open success...");
            return true;
        } catch (InterruptedException e) {
            log.error("the netty server open fail");
        }
        return false;
    }


    private void initBootstrap() {

        this.serverBootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup();
        workGroup = new NioEventLoopGroup();
        serverBootstrap.group(bossGroup, workGroup);
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(encoder);
                ch.pipeline().addLast(decoder);
                ch.pipeline().addLast(remotingHandler);
            }
        });

        serverBootstrap.option(ChannelOption.SO_BACKLOG, 128);       // (5)
        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

    }

    public void close() {

    }

    public RemotingCommand sendSync(RemotingCommand command) {
        return null;
    }


    public static void main(String[] args) {

        RemotingChannel remotingChannel = new NettyServer();
        boolean open = remotingChannel.open("localhost", 8881);
        log.info("the remoting channel open:" + open);
    }
}
