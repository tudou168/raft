package com.tongbanjie.raft.core.remoting.support.netty;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 *
 * @author banxia
 * @date 2017-11-28 10:10:15
 */
public class NettyPooledChannelFactory extends BasePooledObjectFactory<NettyChannel> {


    private final static Logger log = LoggerFactory.getLogger(NettyPooledChannelFactory.class);

    private NettyClient nettyClient;

    public NettyPooledChannelFactory(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }

    public NettyChannel create() throws Exception {
        NettyChannel nettyChannel = new NettyChannel(this.nettyClient);
        nettyChannel.open();
        return nettyChannel;
    }

    public PooledObject<NettyChannel> wrap(NettyChannel channel) {
        return new DefaultPooledObject<NettyChannel>(channel);
    }

    @Override
    public void destroyObject(PooledObject<NettyChannel> channelPooledObject) throws Exception {


        try {

            NettyChannel nettyChannel = channelPooledObject.getObject();
            if (nettyChannel != null) {
                nettyChannel.close();
            }

        } catch (Exception e) {
            log.error("destroyObject fail", e);
        }
    }

    /**
     * 验证通道是否过期
     *
     * @param channelPooledObject
     * @return
     */
    @Override
    public boolean validateObject(PooledObject<NettyChannel> channelPooledObject) {


        try {

            NettyChannel nettyChannel = channelPooledObject.getObject();
            if (nettyChannel != null && nettyChannel.isAvailable()) {
                return true;
            }

        } catch (Exception e) {
            log.error("validate channel fail", e);
        }

        return false;
    }

    @Override
    public void activateObject(PooledObject<NettyChannel> channelPooledObject) throws Exception {

        try {

            NettyChannel nettyChannel = channelPooledObject.getObject();
            if (!nettyChannel.isAvailable()) {
                nettyChannel.open();
            }

        } catch (Exception e) {
            log.error("activate channel fail", e);
        }
    }
}
