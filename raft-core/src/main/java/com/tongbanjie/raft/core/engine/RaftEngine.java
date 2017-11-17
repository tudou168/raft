package com.tongbanjie.raft.core.engine;

import com.tongbanjie.raft.core.config.RaftConfiguration;
import com.tongbanjie.raft.core.constant.RaftConstant;
import com.tongbanjie.raft.core.election.RaftElectionService;
import com.tongbanjie.raft.core.election.handler.ElectionResponseHandler;
import com.tongbanjie.raft.core.election.support.DefaultRaftElectionService;
import com.tongbanjie.raft.core.exception.RaftException;
import com.tongbanjie.raft.core.log.manage.RaftLogService;
import com.tongbanjie.raft.core.peer.RaftPeer;
import com.tongbanjie.raft.core.peer.support.RaftPeerCluster;
import com.tongbanjie.raft.core.protocol.ElectionRequest;
import com.tongbanjie.raft.core.protocol.ElectionResponse;
import com.tongbanjie.raft.core.protocol.ElectionResponseTuple;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.tongbanjie.raft.core.constant.RaftConstant.noLeader;
import static com.tongbanjie.raft.core.constant.RaftConstant.noVoteFor;

/***
 * raft 引擎
 * @author banxia
 * @date 2017-11-17 09:09:53
 */
public class RaftEngine {

    private final static Logger log = LoggerFactory.getLogger(RaftEngine.class);

    //  id
    private String id;

    //  领导
    private String leader;

    //  投票候选人
    private String voteFor;

    //  状态
    private String state;

    // 当前任期
    private long term;

    // 提交的索引号
    private long commitIndex;

    //  raft 配置
    private RaftConfiguration configuration;

    //  raft 日志服务
    private RaftLogService logService;

    private RaftElectionService electionService;

    //  raft 引擎运行状态
    private AtomicInteger running;

    //  任务执行线程池
    private ExecutorService executorService;

    // 任务调度线程池
    private ScheduledExecutorService scheduledExecutorService;

    //  选举超时调度器
    private ScheduledFuture electionTimeoutScheduledFuture;

    //  心跳调度器
    private ScheduledFuture heartbeatScheduledFuture;

    //  并发锁
    private ReadWriteLock lock = new ReentrantReadWriteLock();

    private SecureRandom random = new SecureRandom();

    //  投票列表
    private ConcurrentHashMap<String, Boolean> votes = new ConcurrentHashMap<String, Boolean>();


    public RaftEngine(String id, RaftLogService logService) {
        this.id = id;
        this.logService = logService;
        this.electionService = new DefaultRaftElectionService(this);

    }


    public void setPeers(List<RaftPeer> peers) {

        if (peers == null || peers.isEmpty()) {
            throw new RaftException("peers is not allow null");
        }
        RaftPeerCluster cluster = new RaftPeerCluster();
        Map<String, RaftPeer> raftPeerMap = new HashMap<String, RaftPeer>();

        for (RaftPeer raftPeer : peers) {

            raftPeerMap.put(raftPeer.getId(), raftPeer);
        }

        cluster.setPeers(raftPeerMap);

        this.configuration = new RaftConfiguration(cluster);

    }

    /***
     * 启动
     */
    public void bootstrap() {

        if (this.configuration.getAllPeers().size() == 0) {
            throw new RaftException("raft peers not allow null!");
        }
        initEngine();
    }

    /**
     * 引擎初始化
     */
    public void initEngine() {

        log.info(String.format("raft %s start init....", getId()));
        this.voteFor = RaftConstant.noLeader;
        this.state = RaftConstant.follower;
        this.term = this.logService.getLastTerm();
        this.commitIndex = this.logService.getLastCommittedIndex();
        this.executorService = new ThreadPoolExecutor(RaftConstant.raftThreadNum, RaftConstant.raftThreadNum, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        this.scheduledExecutorService = Executors.newScheduledThreadPool(2);
        this.resetElectionTimeoutTimer();

        log.info(String.format("raft %s start success...", getId()));

    }


    /**
     * 开始选举
     */
    private void startElection() {

        this.lock.writeLock().lock();

        try {

            log.info(String.format("%s start Election...", getId()));
            this.state = RaftConstant.candidate;
            this.voteFor = this.id;
            this.leader = noLeader;
            this.term++;

        } finally {

            this.lock.writeLock().unlock();
        }

        this.votes.clear();
        List<RaftPeer> peers = this.configuration.getAllPeers().expect(this.id).explode();
        if (peers == null || peers.size() == 0) return;

        for (final RaftPeer peer : peers) {

            this.executorService.submit(new Runnable() {
                public void run() {
                    // 选举投票
                    electionVote(peer);
                }
            });
        }

    }

    /**
     * 开始选举投票
     *
     * @param peer
     */
    private void electionVote(RaftPeer peer) {

        this.lock.readLock().lock();
        ElectionRequest electionRequest = null;
        try {

            electionRequest = new ElectionRequest();
            electionRequest.setLastLogTerm(this.logService.getLastTerm());
            electionRequest.setCandidateId(this.id);
            electionRequest.setLastLogIndex(this.logService.getLastIndex());
            electionRequest.setTerm(term);

        } finally {

            this.lock.readLock().unlock();
        }
        //  请求选举
        this.electionService.electionVoteRequest(peer, electionRequest, new SimpleElectionVoteResponseHandler(electionRequest));
    }


    /**
     * 重置选举超时定时器
     */
    private void resetElectionTimeoutTimer() {

        if (this.electionTimeoutScheduledFuture != null && !this.electionTimeoutScheduledFuture.isDone()) {

            this.electionTimeoutScheduledFuture.cancel(true);
        }

        this.electionTimeoutScheduledFuture = this.scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                // 开始选举
                startElection();

            }
        }, RaftConstant.electionTimeoutMs, getElectionTimeoutMS(), TimeUnit.MILLISECONDS);

    }

    /**
     * 停止选举超时定时器
     */
    private void stopElectionTimeoutTimer() {
        if (this.electionTimeoutScheduledFuture != null && !this.electionTimeoutScheduledFuture.isDone()) {

            this.electionTimeoutScheduledFuture.cancel(true);
        }

    }

    /**
     * 重置心跳定时器
     */
    private void resetHeartbeatTimer() {

        if (this.heartbeatScheduledFuture != null && !this.heartbeatScheduledFuture.isDone()) {
            this.heartbeatScheduledFuture.cancel(true);
        }

        this.heartbeatScheduledFuture = this.scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                // TODO 发送

            }
        }, RaftConstant.heartbeatIntervalTimeMs, RaftConstant.heartbeatIntervalTimeMs, TimeUnit.MILLISECONDS);

    }


    /**
     * 停止心跳定时器
     */
    private void stopHeartbeatTimer() {
        if (this.heartbeatScheduledFuture != null && !this.heartbeatScheduledFuture.isDone()) {
            this.heartbeatScheduledFuture.cancel(true);
        }

    }

    /**
     * 选举超时时间(毫秒)
     *
     * @return
     */
    private int getElectionTimeoutMS() {

        return RaftConstant.electionTimeoutMs + random.nextInt(RaftConstant.electionTimeoutMs);
    }


    /*****************************************选举处理部分****************************************************************/
    /**
     * 投票请求响应处理器
     */
    private class SimpleElectionVoteResponseHandler implements ElectionResponseHandler {

        private ElectionRequest electionRequest;

        public SimpleElectionVoteResponseHandler(ElectionRequest electionRequest) {
            this.electionRequest = electionRequest;
        }

        public void handler(RaftPeer raftPeer, ElectionResponseTuple tuple) {

            lock.writeLock().lock();
            log.info(String.format("election vote response:%s", tuple));

            try {

                if (tuple.isSuccess()) {

                    ElectionResponse electionResponse = tuple.getElectionResponse();
                    long electionResponseTerm = electionResponse.getTerm();
                    boolean voteGranted = electionResponse.isVoteGranted();
                    //是否忽略响应
                    if (term != this.electionRequest.getTerm() || !StringUtils.equals(RaftConstant.candidate, state)) {

                        // ignore
                        log.warn(String.format("ignore the election vote response request.term=%s,current.term=%s,election term %s ", electionResponse.getTerm(), term, electionResponseTerm));
                        return;
                    }

                    //
                    if (term > electionResponse.getTerm()) {

                        log.warn(String.format("ignore the election vote response request.term=%s,current.term=%s,election term %s", electionResponse.getTerm(), term, electionResponseTerm));
                        return;

                    }

                    //  有比自己高的任期号
                    if (electionResponseTerm > term) {

                        log.warn(String.format("found election vote response term %s > current term %s", electionResponseTerm, term));
                        state = RaftConstant.follower;
                        term = electionResponse.getTerm();
                        leader = RaftConstant.noLeader;
                        voteFor = RaftConstant.noVoteFor;
                        log.info(String.format("raft:%s become %s, term:%s", getId(), state, term));
                        return;
                    }
                    //  投票通过
                    if (voteGranted) {
                        log.info(String.format("%s vote for me!", raftPeer.getId()));
                        votes.putIfAbsent(tuple.getId(), voteGranted);
                    } else {
                        log.info(String.format("%s not vote  for me!", raftPeer.getId()));
                    }

                    //  获得大多数投票人的认可
                    if (configuration.pass(votes)) { // my win

                        log.info(String.format("I %s  won the election in term:%s...", getId(), term));
                        this.becomeLeader();
                    }

                    // 没有通过继续等待下次选举的到来

                }


            } finally {

                lock.writeLock().unlock();
            }
        }

        /**
         * 晋级为leader
         */
        private void becomeLeader() {

            state = RaftConstant.leader;
            voteFor = noVoteFor;
            leader = id;
            log.info(String.format("%s stop election timeout timer", getId()));
            // 停止选举超时定时器
            stopElectionTimeoutTimer();
            log.info(String.format("%s start send heartbeat .....", getId()));

        }
    }


    public String getId() {
        return id;
    }
}
