package com.tongbanjie.raft.test.log.peer;

import com.tongbanjie.raft.core.engine.RaftEngine;
import com.tongbanjie.raft.core.log.codec.support.Crc32RaftLogCodec;
import com.tongbanjie.raft.core.log.manage.support.DefaultRaftLogService;
import com.tongbanjie.raft.core.log.store.support.DefaultDataStore;
import com.tongbanjie.raft.core.peer.RaftPeer;
import com.tongbanjie.raft.core.peer.support.RpcRaftPeer;
import com.tongbanjie.raft.test.log.BaseTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/***
 *
 * @author banxia
 * @date 2017-11-22 14:14:45
 */
public class PeerTest extends BaseTest {


    @Test
    public void testPeer() throws InterruptedException {

        String localServer = "127.0.0.1:7002";
        String servers = "127.0.0.1:7001";

        Crc32RaftLogCodec crc32RaftLogCodec = new Crc32RaftLogCodec();
        DefaultDataStore defaultDataStore = new DefaultDataStore("/Users/banxia/Desktop/wp/" + localServer, "raft");
        raftLogService = new DefaultRaftLogService(defaultDataStore, crc32RaftLogCodec);
        String[] serverList = servers.split(",");
        List<RaftPeer> peerList = new ArrayList<RaftPeer>();
        for (String server : serverList) {
            RpcRaftPeer peer = new RpcRaftPeer(server);
            peerList.add(peer);
        }

        RaftEngine localEngine = new RaftEngine(localServer, this.raftLogService);
        RaftPeer localPeer = new RpcRaftPeer(localServer);
        localPeer.setRaftEngine(localEngine);
        localPeer.registerServer();
        peerList.add(localPeer);
        localEngine.setPeers(peerList);
        localEngine.bootstrap();

        int i = 0;
        while (true) {

//            byte[] data = ("raft-test" + i).getBytes();
//            if (StringUtils.equals(RaftConstant.noLeader, localEngine.getLeader())) {
//                System.err.println(" not found leader ....");
//
//
//            } else {
//                boolean sec = localEngine.appendLogEntry(data);
//                System.err.println(">>>>>>>>>>>>>>append log entry " + sec + " <<<<<<<<<<<<<");
//            }
//
            Thread.sleep(1000);
        }


    }
}
