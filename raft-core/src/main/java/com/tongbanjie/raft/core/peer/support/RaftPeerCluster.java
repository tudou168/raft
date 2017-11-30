package com.tongbanjie.raft.core.peer.support;

import com.tongbanjie.raft.core.peer.RaftPeer;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * raft peer 集群节点信息
 * @author banxia
 * @date 2017-11-15 17:17:20
 */
public class RaftPeerCluster {


    private Map<String, RaftPeer> peers = new HashMap<String, RaftPeer>();


    public RaftPeerCluster() {

    }

    public RaftPeerCluster(Map<String, RaftPeer> peers) {
        this.peers = peers;
    }


    public Map<String, RaftPeer> getPeers() {
        return peers;
    }

    public void setPeers(Map<String, RaftPeer> peers) {
        this.peers = peers;
    }

    /**
     * @param id
     * @return
     */
    public RaftPeer get(String id) {

        return this.peers.get(id);
    }


    /**
     * 获取 peer集合
     *
     * @return
     */
    public List<RaftPeer> explode() {
        return new ArrayList<RaftPeer>(this.peers.values());
    }


    /**
     * 排除指定的id
     *
     * @param id
     * @return
     */
    public RaftPeerCluster expect(String id) {

        Map<String, RaftPeer> peerMap = new HashMap<String, RaftPeer>();
        for (Map.Entry<String, RaftPeer> entry : this.peers.entrySet()) {

            if (StringUtils.equals(entry.getKey(), id)) {
                continue;
            }
            peerMap.put(entry.getKey(), entry.getValue());

        }
        return new RaftPeerCluster(peerMap);
    }


    /**
     * @return
     */
    public int size() {

        return this.peers.size();
    }


    /**
     * @return
     */
    public int quorum() {

        if (this.peers.isEmpty() || this.size() == 1) {
            return 1;
        }

        return this.size() / 2 + 1;
    }

}
