package com.tongbanjie.raft.test.start;

import com.tongbanjie.raft.core.builder.RaftPeerBuilder;
import com.tongbanjie.raft.core.log.codec.support.Crc32RaftLogCodec;
import com.tongbanjie.raft.core.peer.RaftPeer;


/***
 *
 * @author banxia
 * @date 2017-11-28 14:14:28
 */
public class RaftServerMainBootstrapTest {


    public static void main(String[] args) {
        args = new String[]{"192.168.124.40:6003", "192.168.124.40:6001,192.168.124.40:6002,192.168.124.40:6003", "./log", ".raft", "7003"};
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
