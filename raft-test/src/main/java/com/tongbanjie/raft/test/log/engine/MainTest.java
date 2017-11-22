package com.tongbanjie.raft.test.log.engine;

import com.tongbanjie.raft.core.constant.RaftConstant;
import com.tongbanjie.raft.core.engine.RaftEngine;
import com.tongbanjie.raft.core.peer.RaftPeer;
import com.tongbanjie.raft.core.peer.support.RpcRaftPeer;
import com.tongbanjie.raft.test.log.BaseTest;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/***
 *
 * @author banxia
 * @date 2017-11-17 14:14:56
 */
public class MainTest extends BaseTest {


    @Test
    public void testRaftEngine() throws InterruptedException {


        RaftEngine raftEngine = new RaftEngine("127.0.0.1:8010", this.raftLogService);

        List<RaftPeer> raftPeers = new ArrayList<RaftPeer>();
        for (int a = 1; a < 10; a++) {
            String host = "127.0.0.1";
            int port = 8080 + a;

            RaftEngine raftEngine2 = new RaftEngine(host + ":" + port, this.raftLogService);
            RaftPeer peer = new RpcRaftPeer(host + ":" + port);
            peer.setRaftEngine(raftEngine2);
            raftPeers.add(peer);
        }

        raftEngine.setPeers(raftPeers);

        raftEngine.bootstrap();


        int i = 0;
        while (true) {

            byte[] data = ("raft-test" + i).getBytes();
            if (StringUtils.equals(RaftConstant.noLeader, raftEngine.getLeader())) {
                System.err.println(" not found leader ....");
            }
            boolean sec = raftEngine.appendLogEntry(data);
            System.err.println(">>>>>>>>>>>>>>append log entry " + sec + " <<<<<<<<<<<<<");
            Thread.sleep(1000);
        }

    }

}
