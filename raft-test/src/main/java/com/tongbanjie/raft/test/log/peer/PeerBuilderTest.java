package com.tongbanjie.raft.test.log.peer;

import com.tongbanjie.raft.core.bootstrap.RaftPeerBuilder;
import com.tongbanjie.raft.core.log.codec.support.Crc32RaftLogCodec;
import com.tongbanjie.raft.core.peer.RaftPeer;

/***
 *
 * @author banxia
 * @date 2017-11-28 15:15:12
 */
public class PeerBuilderTest {


    public static void main(String[] args) {

        String localServer = "127.0.0.1:6001";
        String servers = "127.0.0.1:6001,127.0.0.1:6002,127.0.0.1:6003";
        RaftPeerBuilder raftPeerBuilder = new RaftPeerBuilder();
        RaftPeer raftPeer = raftPeerBuilder
                .localServer(localServer)
                .servers(servers)
                .dataStoreFile("./" + localServer)
                .dataStoreFile("." + localServer + "_raft")
                .logCodec(new Crc32RaftLogCodec()).builder();
        raftPeer.bootstrap();
        while (true) ;
    }
}
