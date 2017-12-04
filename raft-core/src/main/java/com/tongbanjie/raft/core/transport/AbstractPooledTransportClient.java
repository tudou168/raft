package com.tongbanjie.raft.core.transport;

import com.tongbanjie.raft.core.transport.exception.TransportException;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 *
 * @author banxia
 * @date 2017-12-02 23:23:20
 */
public abstract class AbstractPooledTransportClient extends AbstractTransportClient {


    private final static Logger log = LoggerFactory.getLogger(AbstractPooledTransportClient.class);

    private GenericObjectPool<TransportCaller> pool;

    private GenericObjectPoolConfig poolConfig;

    private PooledObjectFactory<TransportCaller> factory;

    protected void initPool() {

        this.poolConfig = new GenericObjectPoolConfig();
        this.poolConfig.setMinIdle(2);
        this.poolConfig.setMaxIdle(10);
        this.poolConfig.setTimeBetweenEvictionRunsMillis(60 * 1000 * 10);
        this.poolConfig.setMinEvictableIdleTimeMillis(1000 * 60 * 30);
        this.poolConfig.setSoftMinEvictableIdleTimeMillis(1000 * 60 * 10);
        this.factory = this.createPooledObjectFactory();
        this.pool = new GenericObjectPool<TransportCaller>(this.factory, this.poolConfig);

        for (int a = 0; a < this.poolConfig.getMinIdle(); a++) {
            try {
                this.pool.addObject();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }


    }

    protected abstract PooledObjectFactory<TransportCaller> createPooledObjectFactory();


    protected TransportCaller borrowObject() throws Exception {

        TransportCaller transportChannel = this.pool.borrowObject();
        if (transportChannel != null && transportChannel.isAvailable()) {
            return transportChannel;
        }

        this.invalidateObject(transportChannel);
        throw new TransportException(String.format("%s borrowObject Error", this.getClass().getName()));

    }

    protected void invalidateObject(TransportCaller transportChannel) {


        try {

            this.pool.invalidateObject(transportChannel);
        } catch (Throwable e) {
            log.error(String.format("%s invalidateObject fail", this.getClass().getName()), e);
        }
    }


    protected void returnValue(TransportCaller channel) {

        if (null == channel) return;

        try {

            this.pool.returnObject(channel);

        } catch (Throwable e) {
            log.error(String.format("%s returnValue fail", this.getClass().getName()), e);
        }
    }

}
