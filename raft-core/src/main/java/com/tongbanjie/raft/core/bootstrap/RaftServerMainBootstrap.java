package com.tongbanjie.raft.core.bootstrap;

import com.tongbanjie.raft.core.log.codec.support.Crc32RaftLogCodec;
import com.tongbanjie.raft.core.peer.RaftPeer;

/***
 *
 * @author banxia
 * @date 2017-11-28 14:14:28
 */
public class RaftServerMainBootstrap {


    public static void main(String[] args) {

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
                .dataStorePath(dataStorePath)
                .dataStoreFile(dataStoreFile)
                .logCodec(new Crc32RaftLogCodec()).builder();
        raftPeer.bootstrap();


    }


}
