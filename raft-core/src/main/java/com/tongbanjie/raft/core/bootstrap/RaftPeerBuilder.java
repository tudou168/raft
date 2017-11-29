package com.tongbanjie.raft.core.bootstrap;

import com.tongbanjie.raft.core.constant.RaftConstant;
import com.tongbanjie.raft.core.engine.RaftEngine;
import com.tongbanjie.raft.core.exception.RaftException;
import com.tongbanjie.raft.core.log.codec.RaftLogCodec;
import com.tongbanjie.raft.core.log.manage.support.DefaultRaftLogService;
import com.tongbanjie.raft.core.log.storage.support.DefaultDataStorage;
import com.tongbanjie.raft.core.peer.RaftPeer;
import com.tongbanjie.raft.core.peer.support.RpcRaftPeer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/***
 *
 *
 *            |         |
 * -----------local peer--> server peers
 *            |        |
 *  raft peer 构造器
 * @author banxia
 * @date 2017-11-28 14:14:29
 */
public class RaftPeerBuilder {


    //  本地服务地址
    private String localServer;

    //  集群配置列表
    private String servers;

    // 日志存储路径
    private String dataStorePath;

    //  日志存储文件名称
    private String dataStoreFile;

    //  日志编码及解码器
    private RaftLogCodec logCodec;


    public RaftPeerBuilder localServer(String localServer) {

        this.localServer = localServer;
        return this;
    }

    public RaftPeerBuilder servers(String servers) {
        this.servers = servers;
        return this;
    }


    public RaftPeerBuilder dataStorePath(String dataStorePath) {
        this.dataStorePath = dataStorePath;
        return this;
    }

    public RaftPeerBuilder dataStoreFile(String dataStoreFile) {
        this.dataStoreFile = dataStoreFile;
        return this;
    }


    public RaftPeerBuilder logCodec(RaftLogCodec logCodec) {
        this.logCodec = logCodec;
        return this;
    }


    public RaftPeer builder() {

        this.checkFields();

        System.out.println(RaftConstant.logo);

        DefaultDataStorage dataStorage = new DefaultDataStorage(this.dataStorePath, this.dataStoreFile);

        DefaultRaftLogService logService = new DefaultRaftLogService(dataStorage, this.logCodec);
        List<RaftPeer> raftPeers = this.buildPeers();
        RpcRaftPeer localPeer = new RpcRaftPeer(this.localServer);
        RaftEngine localEngine = new RaftEngine(localServer, logService);
        localPeer.setRaftEngine(localEngine);
        localEngine.setConfiguration(raftPeers, null);
        localPeer.registerServer();
        return localPeer;
    }

    /**
     * 构造非本地 peer 列表
     *
     * @return
     */
    private List<RaftPeer> buildPeers() {

        String[] serverStrList = this.servers.split(",");
        List<RaftPeer> raftPeers = new ArrayList<RaftPeer>();
        for (String server : serverStrList) {

            RaftPeer raftPeer = new RpcRaftPeer(server);
            // register the remoting client
            raftPeer.registerRemotingClient();
            raftPeers.add(raftPeer);
        }


        return raftPeers;
    }

    private void checkFields() {

        if (StringUtils.isBlank(this.localServer)) {
            throw new RaftException("localServer not allow null");
        }
        if (StringUtils.isBlank(servers)) {
            throw new RaftException("servers is not allow null");
        }

        if (!servers.contains(localServer)) {

            throw new RaftException(String.format("servers %s not contains localServer %s", this.servers, this.localServer));
        }
        if (StringUtils.isBlank(this.dataStorePath)) {

            this.dataStorePath = ".";
        }

        if (StringUtils.isBlank(this.dataStoreFile)) {

            this.dataStoreFile = ".raft";
        }


        if (this.logCodec == null) {
            throw new RaftException("the raft log codec not allow null");
        }


    }


}
