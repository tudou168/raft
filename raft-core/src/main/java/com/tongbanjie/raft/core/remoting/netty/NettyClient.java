package com.tongbanjie.raft.core.remoting.netty;

import com.tongbanjie.raft.core.enums.RemotingChannelState;
import com.tongbanjie.raft.core.enums.RemotingCommandState;
import com.tongbanjie.raft.core.enums.RemotingCommandType;
import com.tongbanjie.raft.core.remoting.*;
import com.tongbanjie.raft.core.remoting.netty.codec.RemotingCommandDecoder;
import com.tongbanjie.raft.core.remoting.netty.codec.RemotingCommandEncoder;
import com.tongbanjie.raft.core.remoting.netty.handler.RemotingCommandClientHandler;
import com.tongbanjie.raft.core.util.RequestIdGenerator;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/***
 *
 * @author banxia
 * @date 2017-11-21 15:15:10
 */
public class NettyClient extends AbstractRemotingClient {

    private final static Logger log = LoggerFactory.getLogger(NettyClient.class);

    private final static int MAX_FRAME_LENGTH = 1024 * 1024;

    private final static int LENGTH_FIELD_OFF_SET = 16;

    private final static int LENGTH_FIELD_LENGTH = 4;

    private final static int TIMEOUT = 6000;

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

    private ChannelFuture channelFuture;

    private ConcurrentHashMap<Long, NettyResponseFuture> futureMap = new ConcurrentHashMap<Long, NettyResponseFuture>();


    public void registerCallback(NettyResponseFuture nettyResponseFuture) {

        this.futureMap.put(nettyResponseFuture.getRequestId(), nettyResponseFuture);

    }


    public NettyResponseFuture removeNettyResponseFuture(Long requestId) {

        return this.futureMap.remove(requestId);

    }


    public synchronized boolean open(String host, int port) {


        if (this.state.isInitState()) {
            log.warn(String.format("the netty client [%s:%s] is already open...", host, port));
            return true;
        }

        this.host = host;
        this.port = port;

        // 初始化
        this.initBootstrap();
        log.info(String.format("the netty client [%s:%s]  start  open...", host, port));
        doConnect();
        return true;


    }


    /**
     * 连接
     */
    public void doConnect() {

        if (this.channelFuture != null && this.channelFuture.channel().isActive()) {

            return;
        }

        try {

            channelFuture = bootstrap.connect(host, port).sync();
            channelFuture.addListener(new ChannelFutureListener() {

                public void operationComplete(ChannelFuture future) throws Exception {

                    if (future.isSuccess()) {

                        channelFuture = future;
                        log.info(String.format("Connect to server [%s:%s] successfully!", host, port));
                        state = RemotingChannelState.INIT;

                    } else {
                        log.info(String.format("Connect to server [%s:%s] fail!", host, port));
                        future.channel().eventLoop().schedule(new Runnable() {
                            public void run() {

                                doConnect();
                            }
                        }, 3, TimeUnit.SECONDS);

                        log.info(String.format("Failed to connect to server [%s:%s], try connect after 5s", host, port));
                    }
                }
            });
        } catch (Exception e) {
            log.error(String.format("the netty server [%s:%s] is  open fail...", host, port), e);
            this.doConnect();
        }
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
                        ch.pipeline().addLast(new RemotingCommandClientHandler(NettyClient.this, new MessageHandler() {
                            public void handler(RemotingChannel channel, Object msg) {
                                RemotingCommand command = (RemotingCommand) msg;
                                NettyResponseFuture responseFuture = NettyClient.this.removeNettyResponseFuture(command.getRequestId());
                                if (responseFuture == null) {
                                    return;
                                }
                                if (command.getState() == RemotingCommandState.SUCCESS.getValue()) {
                                    responseFuture.onSuccess(command);
                                } else if (command.getState() == RemotingCommandState.SUCCESS.getValue()) {
                                    responseFuture.onFail(command);
                                }

                            }
                        }));
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

            this.channelFuture.channel().closeFuture().sync();
            this.workerGroup.shutdownGracefully();
        } catch (InterruptedException e) {
            log.error("the netty  close fail", e);
            throw new RuntimeException("the netty  close fail", e);
        }


    }

    public boolean isClosed() {

        return this.state.isClosedState();
    }

    public RemotingCommand request(RemotingCommand command) {

        NettyResponseFuture responseFuture = new NettyResponseFuture(command, TIMEOUT);
        this.registerCallback(responseFuture);
        ChannelFuture writeFuture = this.channelFuture.channel().writeAndFlush(command);
        boolean result = writeFuture.awaitUninterruptibly(TIMEOUT);
        if (result && writeFuture.isSuccess()) {

            log.info("request success responseFuture:" + responseFuture + ",request:" + command);
            return responseFuture.getRemotingCommand();
        }

        // fail
        writeFuture.cancel(false);
        NettyResponseFuture response = this.removeNettyResponseFuture(command.getRequestId());

        if (response != null) {
            response.cancel();
        }


        throw new RuntimeException("request fail  request :" + command);

    }


    public static void main(String[] args) {

        NettyClient nettyClient = new NettyClient();
        nettyClient.open("127.0.0.1", 8181);

        for (; ; ) {

            try {

                RemotingCommand command = new RemotingCommand();
                command.setBody("动态");
                command.setRequestId(RequestIdGenerator.getRequestId());
                command.setCommandType(RemotingCommandType.ELECTION.getValue());
                command.setState(RemotingCommandState.SUCCESS.getValue());
                ChannelFuture channelFuture = nettyClient.channelFuture;
                if (channelFuture != null && channelFuture.channel().isActive()) {
                    RemotingCommand response = nettyClient.request(command);
                    log.info(">>>>>>>>>>请求响应结果:response:" + response);
                }


                Thread.sleep(1000);
            } catch (Exception e) {

            }

        }

    }
}
