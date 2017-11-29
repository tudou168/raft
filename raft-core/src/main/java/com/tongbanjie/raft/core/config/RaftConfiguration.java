package com.tongbanjie.raft.core.config;

import com.tongbanjie.raft.core.enums.RaftConfigurationState;
import com.tongbanjie.raft.core.exception.RaftException;
import com.tongbanjie.raft.core.peer.RaftPeer;
import com.tongbanjie.raft.core.peer.support.RaftPeerCluster;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/***
 * raft 配置管理
 * @author banxia
 * @date 2017-11-15 16:16:54
 */
public class RaftConfiguration {

    private final static Logger log = LoggerFactory.getLogger(RaftConfiguration.class);


    private RaftPeerCluster oldPeers;
    private RaftPeerCluster newPeers;

    private String state;

    private ReadWriteLock lock = new ReentrantReadWriteLock();


    public RaftConfiguration() {

    }


    /**
     * 直接设置peers
     *
     * @param peers
     */
    public void directSetPeers(RaftPeerCluster peers) {

        this.lock.writeLock().lock();

        try {

            this.oldPeers = peers;
            this.state = RaftConfigurationState.COLD.getName();
            this.newPeers = new RaftPeerCluster();
        } finally {
            this.lock.writeLock().unlock();
        }
    }


    /**
     * 根据id 获取对应的peer信息
     *
     * @param id
     * @return
     */
    public RaftPeer get(String id) {


        this.lock.readLock().lock();

        try {
            //  从老的配置集合获取
            RaftPeer raftPeer = this.oldPeers.get(id);
            if (raftPeer != null) return raftPeer;
            //  从新的配置集合中获取
            raftPeer = this.newPeers.get(id);
            return raftPeer;
        } finally {
            this.lock.readLock().unlock();
        }
    }


    /**
     * 获取所有的 peer 集合
     *
     * @return
     */
    public RaftPeerCluster getAllPeers() {


        this.lock.readLock().lock();

        try {

            Map<String, RaftPeer> peerClusterMap = new HashMap<String, RaftPeer>();


            Map<String, RaftPeer> oldPeers = this.oldPeers.getPeers();

            peerClusterMap.putAll(oldPeers);

            Map<String, RaftPeer> newPeers = this.newPeers.getPeers();
            peerClusterMap.putAll(newPeers);
            return new RaftPeerCluster(peerClusterMap);
        } finally {
            this.lock.readLock().unlock();
        }
    }


    /**
     * 判断投票是否超过半数
     *
     * @param votes
     * @return true 超过半数   false 没有超过
     */
    public boolean pass(Map<String, Boolean> votes) {

        this.lock.readLock().lock();

        try {

            int oldHave = 0;


            int oldQuorum = this.oldPeers.quorum();

            for (Map.Entry<String, Boolean> entry : votes.entrySet()) {

                if (entry.getValue().equals(true)) {
                    oldHave++;
                }

                if (oldHave == oldQuorum) {
                    break;
                }

            }


            if (oldHave < oldQuorum) {

                return false;
            }

            if (StringUtils.equals(RaftConfigurationState.COLD.getName(), this.state)) {

                return true;
            }


            int newHave = 0;

            int newQuorum = this.newPeers.quorum();
            for (Map.Entry<String, Boolean> entry : votes.entrySet()) {

                if (entry.getValue().equals(true)) {
                    newHave++;
                }

                if (newHave == newQuorum) {
                    break;
                }

            }

            return newHave >= newQuorum;
        } finally {
            this.lock.readLock().unlock();
        }

    }


    /***
     * 变更新raft配置
     * @param peerCluster
     */
    public void changeTo(RaftPeerCluster peerCluster) {

        this.lock.writeLock().lock();

        try {

            if (!StringUtils.equals(RaftConfigurationState.COLD.getName(), this.state)) {

                throw new RaftException(String.format("the raft configuration state is not %s", RaftConfigurationState.CNEW.getName()));
            }

            if (this.newPeers.size() > 0) {

                throw new RaftException(String.format("the raft configuration new configuration is not empty %s", this.newPeers.getPeers()));
            }

            this.newPeers = peerCluster;
            this.state = RaftConfigurationState.CNEW.getName();

        } finally {
            this.lock.writeLock().unlock();
        }
    }


    /**
     * 提交raft配置
     */
    public void commitConfigurationTo() {

        this.lock.writeLock().lock();

        try {

            if (!StringUtils.equals(this.state, RaftConfigurationState.CNEW.getName())) {
                log.warn("the raft configuration state is not Cnew state !");
                return;
            }

            if (this.newPeers.size() == 0) {

                throw new RaftException("raft configuration new peers is empty !");
            }

            //  destroy all  old Peers client
            for (RaftPeer peer : oldPeers.explode()) {

                try {

                    peer.unregisterRemotingClient();

                } catch (Exception e) {
                    log.error(String.format(" peer %s unregisterRemotingClient fail:%s", peer.getId(), e.getMessage()), e);

                }
            }

            this.oldPeers = this.newPeers;
            this.state = RaftConfigurationState.COLD.getName();
            this.newPeers = new RaftPeerCluster();
        } finally {
            this.lock.writeLock().unlock();
        }
    }


    /**
     * 取消变更
     */
    public void changeAbort() {

        this.lock.writeLock().lock();

        try {

            if (!StringUtils.equals(this.state, RaftConfigurationState.CNEW.getName())) {

                log.warn("raft config state is not cnew state !");
                return;
            }


            //  destroy all  new peers client
            for (RaftPeer peer : newPeers.explode()) {

                try {

                    peer.unregisterRemotingClient();

                } catch (Exception e) {
                    log.error(String.format(" peer %s unregisterRemotingClient fail:%s", peer.getId(), e.getMessage()), e);

                }
            }

            this.newPeers = new RaftPeerCluster();
            this.state = RaftConfigurationState.COLD.getName();

        } finally {
            this.lock.writeLock().unlock();
        }
    }


    /**
     * check contains peer
     *
     * @param peerId peer id
     * @return
     */
    public boolean containsPeer(String peerId) {

        this.lock.readLock().lock();

        try {
            return this.getAllPeers().getPeers().containsKey(peerId);
        } finally {

            this.lock.readLock().unlock();
        }


    }


    public RaftPeerCluster getOldPeers() {
        return oldPeers;
    }

    public RaftPeerCluster getNewPeers() {
        return newPeers;
    }

    public String getState() {
        return state;
    }
}
