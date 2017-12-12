package com.tongbanjie.raft.core.transport.netty;

import com.tongbanjie.raft.core.transport.exception.TransportException;
import com.tongbanjie.raft.core.transport.exception.TransportTimeoutException;
import com.tongbanjie.raft.core.transport.*;
import com.tongbanjie.raft.core.transport.enums.ChannelType;
import com.tongbanjie.raft.core.transport.enums.TransportChannelState;
import com.tongbanjie.raft.core.transport.netty.codec.NettyDecoder;
import com.tongbanjie.raft.core.transport.netty.codec.NettyEncoder;
import com.tongbanjie.raft.core.transport.netty.serialization.Serialization;
import com.tongbanjie.raft.core.transport.proxy.TransportClientProxy;
import com.tongbanjie.raft.core.transport.proxy.support.JdkTransportClientProxy;
import com.tongbanjie.raft.core.util.NetUtil;
import com.tongbanjie.raft.core.util.RequestIdGenerator;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.pool2.PooledObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/***
 *  基于 netty client
 * @author banxia
 * @date 2017-12-02 23:23:42
 */
public class NettyClient<T> extends AbstractPooledTransportClient {


    private final static Logger log = LoggerFactory.getLogger(NettyClient.class);


    private String host;

    private int port;

    private Class<T> serviceInterface;

    private TransportClientProxy transportClientProxy;

    private T proxy;

    private Bootstrap bootstrap;

    private EventLoopGroup workerGroup;

    private ConcurrentHashMap<Long, NettyResponseFuture> callbackMap = new ConcurrentHashMap<Long, NettyResponseFuture>();

    private Serialization serialization;

    private long requestTimeout;

    public Bootstrap getBootstrap() {
        return bootstrap;
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

    public Class<T> getServiceInterface() {
        return serviceInterface;
    }

    public long getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(long requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public void setServiceInterface(Class<T> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public TransportClientProxy getTransportClientProxy() {
        return transportClientProxy;
    }

    public void setTransportClientProxy(TransportClientProxy transportClientProxy) {
        this.transportClientProxy = transportClientProxy;
    }

    public Serialization getSerialization() {
        return serialization;
    }

    public void setSerialization(Serialization serialization) {
        this.serialization = serialization;
    }

    @Override
    protected PooledObjectFactory<TransportCaller> createPooledObjectFactory() {
        return new PooledNettyChannelFactory(this);
    }

    @Override
    public boolean open() {

        if (this.isAvailable()) {
            log.warn(String.format("the nettyClient %s:%s has already open", host, port));
            return true;
        }


        this.baseCheck();

        initBootstrap();
        // init the channel pool
        this.initPool();
        log.info(String.format("the nettyClient %s:%s open success", host, port));
        this.state = TransportChannelState.ALIVE;
        return true;


    }

    private void initBootstrap() {
        this.bootstrap = new Bootstrap();
        this.workerGroup = new NioEventLoopGroup();
        this.bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {

                        ch.pipeline().addLast("encoder", new NettyEncoder(serialization));
                        ch.pipeline().addLast("decoder", new NettyDecoder(1024 * 1024, 0, 4, serialization, ChannelType.CLIENT));
                        ch.pipeline().addLast("handler", new SimpleChannelInboundHandler<Response>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, Response msg) throws Exception {

                                long requestId = msg.getRequestId();

                                NettyResponseFuture responseFuture = unregisterCallback(requestId);
                                if (null != responseFuture) {

                                    if (msg.isSuccess()) {
                                        responseFuture.onSuccess(msg);
                                    } else {
                                        responseFuture.onFailure(msg);
                                    }

                                }

                            }
                        });
                    }
                });
    }

    private void baseCheck() {

        if (StringUtils.isBlank(host)) {
            this.host = NetUtil.getLocalAddress().getHostAddress();
        }

        if (this.port <= 0) {
            throw new TransportException(String.format("the nettyClient  port %s is invalid", this.port));
        }


    }

    @Override
    public void close() {

        if (this.state.isClosedState()) {
            log.info(String.format("the nettyClient %s:%s has already close", host, port));
            return;
        }

        if (this.state.isUinitState()) {
            log.info(String.format("the nettyClient %s:%s has no init", host, port));
            return;
        }

        this.workerGroup.shutdownGracefully();
        this.state = TransportChannelState.CLOSED;
    }

    @Override
    public boolean isClosed() {
        return state.isClosedState();
    }

    @Override
    public boolean isAvailable() {
        return this.state.isAliveState();
    }

    @Override
    public <T> T getProxy(Class<T> serviceInterface) {
        if (this.transportClientProxy == null) {
            this.transportClientProxy = new JdkTransportClientProxy();
        }
        if (this.proxy == null) {
            this.proxy = transportClientProxy.getProxyClient(this.serviceInterface, this);
        }
        return (T) this.proxy;
    }


    /**
     * register the callback future
     *
     * @param requestId
     * @param future
     */
    public void registerCallback(long requestId, NettyResponseFuture future) {
        this.callbackMap.put(requestId, future);
    }

    /**
     * unregister the callback future
     *
     * @param requestId
     * @return
     */
    public NettyResponseFuture unregisterCallback(long requestId) {

        return this.callbackMap.remove(requestId);
    }

    @Override
    public Object request(Method method, Object[] arguments) {

        TransportCaller caller = null;
        ResponseFuture responseFuture = null;
        Object result = null;

        // ready the request
        Request request = new Request();
        request.setMethod(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setArguments(arguments);
        request.setServiceInterface(this.serviceInterface);
        request.setRequestId(RequestIdGenerator.getRequestId());

        try {


            caller = this.borrowObject();
            if (caller == null) {

                throw new TransportException(String.format("NettyClient borrowObject null request=%s", request));
            }

            responseFuture = caller.request(request);

            result = responseFuture.getValue();

            // return the caller to the pool
            this.returnValue(caller);

        } catch (Exception e) {
            log.error(String.format("NettyClient request Error request=%s", request), e);
            // check the caller
            this.invalidateObject(caller);

            if (e instanceof TransportTimeoutException) {
                throw (TransportTimeoutException) e;
            } else {
                throw new TransportException(String.format("nettyClient request Error %s", request), e);
            }

        }
        return result;
    }
}
