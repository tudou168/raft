package com.tongbanjie.raft.test.start;

import com.tongbanjie.raft.core.client.RaftClient;
import com.tongbanjie.raft.core.client.RaftClientBuilder;
import com.tongbanjie.raft.core.peer.support.server.RaftClientService;
import com.tongbanjie.raft.core.protocol.JoinResponse;
import com.tongbanjie.raft.core.transport.builder.NettyClientBuilder;
import com.tongbanjie.raft.core.transport.netty.serialization.support.Hessian2Serialization;
import com.tongbanjie.raft.core.transport.proxy.support.JdkTransportClientProxy;
import org.junit.Test;

/***
 * raft client main class
 * @author banxia
 * @date 2017-11-30 15:15:44
 */
public class RaftClientMainBootstrapTest {


    private RaftClient raftClient;

    @Test
    public void joinTest() {
        String host = "192.168.124.40";
        Integer port = 7001;
        String server = host + ":" + port;
        NettyClientBuilder<RaftClientService> nettyClientBuilder = new NettyClientBuilder<RaftClientService>();
        RaftClientService raftClientService = nettyClientBuilder.port(port).host(host)
                .serialization(new Hessian2Serialization())
                .serviceInterface(RaftClientService.class)
                .requestTimeout(6000)
                .transportClientProxy(new JdkTransportClientProxy()).builder();

        this.raftClient = new RaftClient(raftClientService, nettyClientBuilder.getTransportClient());
        System.out.println("Connecting to " + server + " success!");
        JoinResponse joinResponse = raftClient.joinCluster("192.168.124.40:6003");
        System.out.println(joinResponse.getReason());
        System.out.println(joinResponse.isSuccess());
    }

}
