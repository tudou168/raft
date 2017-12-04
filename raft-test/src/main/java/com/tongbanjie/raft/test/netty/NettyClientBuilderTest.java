package com.tongbanjie.raft.test.netty;

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

        NettyClientBuilder<EchoService> nettyClientBuilder = new NettyClientBuilder<EchoService>();
        EchoService echoService = nettyClientBuilder.port(7890)
                .serialization(new Hessian2Serialization())
                .serviceInterface(EchoService.class)
                .requestTimeout(6000)
                .transportClientProxy(new JdkTransportClientProxy()).builder();


        while (true) {

            String echo = echoService.echo("raft");
            System.err.println(echo);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
