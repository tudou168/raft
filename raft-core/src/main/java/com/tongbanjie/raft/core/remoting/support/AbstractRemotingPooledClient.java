package com.tongbanjie.raft.core.remoting.support;

import com.tongbanjie.raft.core.enums.RemotingChannelState;
import com.tongbanjie.raft.core.remoting.RemotingChannel;
import com.tongbanjie.raft.core.remoting.RemotingClient;
import com.tongbanjie.raft.core.remoting.support.netty.NettyChannel;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 *
 * @author banxia
 * @date 2017-11-21 14:14:37
 */
public abstract class AbstractRemotingPooledClient extends AbstractRemotingClient {


    private final static Logger log = LoggerFactory.getLogger(AbstractRemotingPooledClient.class);


    private GenericObjectPool<NettyChannel> pool;
    private GenericObjectPoolConfig poolConfig;

    private PooledObjectFactory factory;


    protected void initPool() {

        this.poolConfig = new GenericObjectPoolConfig();
        this.poolConfig.setMinIdle(1);
        this.poolConfig.setMaxIdle(5);
        this.poolConfig.setMaxTotal(50);
        this.poolConfig.setMaxWaitMillis(6000);
        this.poolConfig.setTimeBetweenEvictionRunsMillis(60 * 60 * 10);
        this.factory = this.newNettyChannelPooledFactory();
        this.pool = new GenericObjectPool(this.factory, this.poolConfig);

        for (int a = 0; a < this.pool.getMinIdle(); a++) {

            try {
                this.pool.addObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    protected abstract PooledObjectFactory newNettyChannelPooledFactory();


    protected NettyChannel borrowChannel() throws Exception {
        NettyChannel nettyChannel = this.pool.borrowObject();
        if (nettyChannel != null && nettyChannel.isAvailable()) {
            return nettyChannel;
        }

        this.invalidateChannel(nettyChannel);

        throw new RuntimeException("borrow channel fail");


    }


    protected void invalidateChannel(NettyChannel channel) {
        if (channel == null) return;

        try {
            pool.invalidateObject(channel);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }


    protected void returnChannel(NettyChannel channel) {

        if (channel == null) return;
        try {

            this.pool.returnObject(channel);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }


}
