package com.tongbanjie.raft.test.start;

import com.tongbanjie.raft.core.constant.RaftConstant;
import com.tongbanjie.raft.core.engine.RaftEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 *
 * @author banxia
 * @date 2017-12-11 11:11:41
 */
public class RaftMainStartTest {

    private final static Logger log = LoggerFactory.getLogger(RaftMainStartTest.class);


    public static void main(String[] args) {

        log.info(RaftConstant.logo);
        String servers = "127.0.0.1:6003";
        String localServer = "127.0.0.1:6003";
        int clientPort = 7003;
        RaftEngine raftEngine = new RaftEngine(localServer, servers, "./data", clientPort);

        raftEngine.bootstrap();


    }
}
