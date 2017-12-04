package com.tongbanjie.raft.core.transport.netty;

import com.tongbanjie.raft.core.transport.TransportCaller;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/***
 *
 * @author banxia
 * @date 2017-12-03 23:23:08
 */
public class PooledNettyChannelFactory extends BasePooledObjectFactory<TransportCaller> {


    private NettyClient nettyClient;

    public PooledNettyChannelFactory(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }


    @Override
    public TransportCaller create() throws Exception {
        NettyChannel nettyChannel = new NettyChannel(this.nettyClient);
        nettyChannel.open();
        return nettyChannel;
    }

    @Override
    public PooledObject<TransportCaller> wrap(TransportCaller obj) {
        return new DefaultPooledObject<TransportCaller>(obj);
    }

    @Override
    public void destroyObject(PooledObject<TransportCaller> p) throws Exception {

        try {

            TransportCaller transportCaller = p.getObject();
            transportCaller.close();
        } catch (Exception e) {


        }
    }

    @Override
    public boolean validateObject(PooledObject<TransportCaller> p) {

        try {

            TransportCaller transportCaller = p.getObject();
            return transportCaller.isAvailable();
        } catch (Exception e) {

        }

        return false;
    }


    @Override
    public void activateObject(PooledObject<TransportCaller> p) throws Exception {

        try {

            TransportCaller transportCaller = p.getObject();
            if (!transportCaller.isAvailable()) {
                transportCaller.open();
            }

        } catch (Exception e) {

        }
    }
}
