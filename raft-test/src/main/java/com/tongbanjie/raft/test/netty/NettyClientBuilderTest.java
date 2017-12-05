package com.tongbanjie.raft.test.netty;

import com.tongbanjie.raft.core.peer.support.server.RaftClientService;
import com.tongbanjie.raft.core.protocol.JoinResponse;
import com.tongbanjie.raft.core.transport.builder.NettyClientBuilder;
import com.tongbanjie.raft.core.transport.netty.serialization.support.Hessian2Serialization;
import com.tongbanjie.raft.core.transport.proxy.support.JdkTransportClientProxy;
import org.junit.Test;

/***
 *
 * @author banxia
 * @date 2017-12-04 16:16:41
 */
public class NettyClientBuilderTest {

    @Test
    public void test() {

        NettyClientBuilder<RaftClientService> nettyClientBuilder = new NettyClientBuilder<RaftClientService>();
        RaftClientService echoService = nettyClientBuilder.port(7001)
                .serialization(new Hessian2Serialization())
                .serviceInterface(RaftClientService.class)
                .requestTimeout(6000)
                .transportClientProxy(new JdkTransportClientProxy()).builder();


        while (true) {

            JoinResponse joinResponse = echoService.joinCluster("raft");
            System.err.println(joinResponse);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
