package com.tongbanjie.raft.core.bootstrap;

import com.tongbanjie.raft.core.constant.RaftConstant;
import com.tongbanjie.raft.core.engine.RaftEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 *
 * @author banxia
 * @date 2017-11-28 14:14:28
 */
public class RaftServerMainBootstrap {

    private static Logger log = LoggerFactory.getLogger(RaftServerMainBootstrap.class);

    public static void main(String[] args) {

        log.info(RaftConstant.logo);
        String localServer = args[0];
        String servers = args[1];
        String raftDir = args[2];
        int clientPort = Integer.valueOf(args[3]);

        RaftEngine raftEngine = new RaftEngine(localServer, servers, raftDir, clientPort);
        raftEngine.bootstrap();

    }


}
