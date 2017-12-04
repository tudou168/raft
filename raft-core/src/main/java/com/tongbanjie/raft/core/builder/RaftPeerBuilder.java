package com.tongbanjie.raft.core.builder;

import com.tongbanjie.raft.core.constant.RaftConstant;
import com.tongbanjie.raft.core.engine.RaftEngine;
import com.tongbanjie.raft.core.exception.RaftException;
import com.tongbanjie.raft.core.log.codec.RaftLogCodec;
import com.tongbanjie.raft.core.log.manage.support.DefaultRaftLogService;
import com.tongbanjie.raft.core.log.storage.support.DefaultDataStorage;
import com.tongbanjie.raft.core.peer.RaftPeer;
import com.tongbanjie.raft.core.peer.support.RpcRaftPeer;
import com.tongbanjie.raft.core.util.NetUtil;
import org.apache.commons.lang.StringUtils;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/***
 *
 * @author banxia
 * @date 2017-12-04 17:17:39
 */
public class RaftPeerBuilder {

    //  本地服务地址
    private String localServer;

    private Integer clientPort;

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

    public RaftPeerBuilder clientPort(int clientPort) {

        this.clientPort = clientPort;
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

        RpcRaftPeer localPeer = new RpcRaftPeer(this.localServer);

        // register local server  accept the client connect
        this.registerLocalServer(localPeer);

        localPeer.registerRaftTransportServer();

        List<RaftPeer> raftPeers = this.buildPeers();
        RaftEngine localEngine = new RaftEngine(localServer, logService);
        localPeer.setRaftEngine(localEngine);
        localEngine.setConfiguration(raftPeers, null);

        return localPeer;
    }

    /**
     * 注册本地服务
     *
     * @param localPeer
     */
    private void registerLocalServer(RaftPeer localPeer) {

        InetAddress localAddress = NetUtil.getLocalAddress();
        String host = localAddress.getHostAddress();
        localPeer.registerRaftClientTransportServer(host, clientPort);

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
            raftPeer.registerRaftTransportClient();
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

        if (this.clientPort == null) {

            throw new RaftException("the local client server not allow null");
        }


    }
}
