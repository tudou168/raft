package com.tongbanjie.raft.test.netty;

import com.tongbanjie.raft.core.transport.TransportServer;
import com.tongbanjie.raft.core.transport.builder.NettyServerBuilder;
import com.tongbanjie.raft.core.transport.netty.serialization.support.Hessian2Serialization;
import org.junit.Test;

/***
 *
 * @author banxia
 * @date 2017-12-04 15:15:55
 */
public class NettyServerBuilderTest {


    @Test
    public void test() {

        NettyServerBuilder<EchoService> builder = new NettyServerBuilder<EchoService>();
        TransportServer transportServer = builder.port(7890)
                .ref(new EchoServiceImpl())
                .serialization(new Hessian2Serialization())
                .threadNum(Runtime.getRuntime().availableProcessors())
                .builder();
        System.err.println("start success..." + transportServer);
        while (true) ;

    }
}
