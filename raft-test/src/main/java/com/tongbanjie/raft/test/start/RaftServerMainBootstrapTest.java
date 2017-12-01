package com.tongbanjie.raft.test.start;

import com.tongbanjie.raft.core.bootstrap.RaftPeerBuilder;
import com.tongbanjie.raft.core.log.codec.support.Crc32RaftLogCodec;
import com.tongbanjie.raft.core.peer.RaftPeer;

import java.io.File;

/***
 *
 * @author banxia
 * @date 2017-11-28 14:14:28
 */
public class RaftServerMainBootstrapTest {


    public static void main(String[] args) {
        args = new String[]{"192.168.124.51:6001", "192.168.124.51:6001,192.168.124.51:6002,192.168.124.51:6003", "./log", ".raft", "7001"};
        if (args == null || args.length < 4) {
            System.err.println("args has no enough!");
            System.exit(1);
        }

        String localServer = args[0];

        String servers = args[1];

        String dataStorePath = args[2];

        String dataStoreFile = args[3];

        Integer clientPort = Integer.valueOf(args[4]);

        RaftPeerBuilder raftPeerBuilder = new RaftPeerBuilder();
        RaftPeer raftPeer = raftPeerBuilder
                .localServer(localServer)
                .servers(servers)
                .clientPort(clientPort)
                .dataStorePath(dataStorePath + "/" + args[0])
                .dataStoreFile(dataStoreFile)
                .logCodec(new Crc32RaftLogCodec()).builder();
        raftPeer.bootstrap();


    }


}
