package com.tongbanjie.raft.test.log.engine;

import com.tongbanjie.raft.core.engine.RaftEngine;
import com.tongbanjie.raft.core.peer.RaftPeer;
import com.tongbanjie.raft.core.peer.support.RpcRaftPeer;
import com.tongbanjie.raft.test.log.BaseTest;
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
    public void testRaftEngine() {


        RaftEngine raftEngine = new RaftEngine("127.0.0.1:8010", this.raftLogService);

        List<RaftPeer> raftPeers = new ArrayList<RaftPeer>();
        for (int a = 1; a < 10; a++) {
            RaftEngine raftEngine2 = new RaftEngine("127.0.0.1" + a + ":8010", this.raftLogService);
            RaftPeer raftPeer = new RpcRaftPeer(raftEngine2);
            raftPeers.add(raftPeer);
        }

        raftEngine.setPeers(raftPeers);

        raftEngine.bootstrap();

        while (true) ;

    }

}
