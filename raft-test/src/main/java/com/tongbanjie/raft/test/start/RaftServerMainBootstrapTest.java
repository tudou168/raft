package com.tongbanjie.raft.test.start;

import com.tongbanjie.raft.core.bootstrap.RaftPeerBuilder;
import com.tongbanjie.raft.core.log.codec.support.Crc32RaftLogCodec;
import com.tongbanjie.raft.core.peer.RaftPeer;

/***
 *
 * @author banxia
 * @date 2017-11-28 14:14:28
 */
public class RaftServerMainBootstrapTest {


    public static void main(String[] args) {
        args = new String[]{"192.168.124.51:6001", "192.168.124.51:6001,192.168.1.121:6001", "./log", ".raft"};
        if (args == null || args.length < 4) {
            System.err.println("args has no enough!");
            System.exit(1);
        }

        String localServer = args[0];

        String servers = args[1];

        String dataStorePath = args[2];

        String dataStoreFile = args[3];


        RaftPeerBuilder raftPeerBuilder = new RaftPeerBuilder();
        RaftPeer raftPeer = raftPeerBuilder
                .localServer(localServer)
                .servers(servers)
                .clientPort(5001)
                .dataStorePath(dataStorePath)
                .dataStoreFile(dataStoreFile)
                .logCodec(new Crc32RaftLogCodec()).builder();
        raftPeer.bootstrap();


    }


}
