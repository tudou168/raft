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
import com.tongbanjie.raft.core.protocol.*;
import com.tongbanjie.raft.core.replication.ReplicationService;
import com.tongbanjie.raft.core.replication.handler.ReplicationLogResponseHandler;
import com.tongbanjie.raft.core.replication.support.DefaultReplicationService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.tongbanjie.raft.core.constant.RaftConstant.noLeader;
import static com.tongbanjie.raft.core.constant.RaftConstant.noVoteFor;

/***
 * raft 核心调度引擎
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

    //  raft 配置
    private RaftConfiguration configuration;

    //  raft 日志服务
    private RaftLogService logService;

    private RaftElectionService electionService;

    private ReplicationService replicationService;

    //  raft 引擎运行状态
    private AtomicInteger running;

    //  任务执行线程池
    private ExecutorService executorService;

    //  日志刷新调度线程
    private ScheduledExecutorService refreshScheduledExecutorService;


    // 任务调度线程池
    private ScheduledExecutorService scheduledExecutorService;

    //  选举超时调度器
    private ScheduledFuture electionTimeoutScheduledFuture;

    //  心跳调度器
    private ScheduledFuture heartbeatScheduledFuture;

    //  日志并发刷新调度器
    private ScheduledFuture replicationScheduledFuture;

    //  并发锁
    private ReadWriteLock lock = new ReentrantReadWriteLock();

    private Condition waitForDoneCondition = lock.writeLock().newCondition();

    private SecureRandom random = new SecureRandom();

    //  用于保存各个 peer 刷新的索引号
    private NextIndex nextIndexList;

    //  投票列表
    private ConcurrentHashMap<String, Boolean> votes = new ConcurrentHashMap<String, Boolean>();

    /***
     *  统计并发刷新日志状态
     */
    private ConcurrentHashMap<String, Boolean> statistics = new ConcurrentHashMap<String, Boolean>();

    // 开始统计标识
    private boolean startStatistic;


    public RaftEngine(String id, RaftLogService logService) {
        this.id = id;
        this.logService = logService;
        this.electionService = new DefaultRaftElectionService(this);
        this.replicationService = new DefaultReplicationService(this);
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
        this.executorService = new ThreadPoolExecutor(RaftConstant.raftThreadNum, RaftConstant.raftThreadNum, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        this.refreshScheduledExecutorService = Executors.newScheduledThreadPool(2);
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

            log.info(String.format("%s become candidate...", getId()));
            this.state = RaftConstant.candidate;
            this.voteFor = this.id;
            this.leader = noLeader;
            this.term++;
            log.info(String.format("%s start Election with term=%s...", getId(), this.term));

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


    /***
     * 开始发送心跳
     */
    private void startHeartbeat() {

        log.info(String.format(">>>>>>>>>>>%s send heartbeat ...<<<<<<<<<<<", getId()));
    }


    /***
     * 追加日志
     *
     * @return true 成功 false 失败
     */
    public boolean appendLogEntry(byte[] data) {


        this.lock.writeLock().lock();

        try {
            log.info(String.format("%s into append log entry ...", getId()));

            if (!StringUtils.equals(RaftConstant.leader, this.state)) {
                log.warn(String.format("%s is not leader !", getId()));
                return false;
            }
            this.startStatistic = true;
            this.statistics = new ConcurrentHashMap<String, Boolean>();
            long lastIndex = this.logService.getLastIndex();
            lastIndex = lastIndex + 1;
            RaftLog raftLog = new RaftLog();
            raftLog.setTerm(term);
            raftLog.setContent(data);
            raftLog.setIndex(lastIndex);
            //  首先将追加到本地日志中
            this.logService.appendRaftLog(raftLog);


            this.waitForDoneCondition.await(RaftConstant.waitForMaxTimeMs, TimeUnit.MILLISECONDS);
            long lastCommittedIndex = this.logService.getLastCommittedIndex();
            if (lastCommittedIndex < lastIndex) {
                log.info(String.format("%s append log entry fail ", getId()));
                return false;
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            return false;
        } finally {
            this.lock.writeLock().unlock();
            this.startStatistic = false;
        }
        return true;

    }


    /**
     * 选举处理
     *
     * @param electionRequest
     * @return
     */
    public ElectionResponse electionVoteHandler(ElectionRequest electionRequest) {

        this.lock.writeLock().lock();
        ElectionResponse electionResponse = new ElectionResponse();
        try {

            electionResponse = new ElectionResponse();

            long requestTerm = electionRequest.getTerm();
            long lastLogIndex = electionRequest.getLastLogIndex();
            long lastLogTerm = electionRequest.getLastLogTerm();
            String candidateId = electionRequest.getCandidateId();

            // 判断当前任期是否大于请求的任期

            if (this.term > requestTerm) {
                electionResponse.setTerm(this.term);
                electionResponse.setVoteGranted(false);
                electionResponse.setReason(String.format("Term %s < %s", requestTerm, this.term));
                return electionResponse;
            }

            // found request.term > term
            boolean stepDown = false;
            if (requestTerm > this.term) {

                log.warn(String.format("%s found request.term %s > current.term %s", getId(), requestTerm, this.term));
                this.term = requestTerm;
                this.leader = noLeader;
                this.voteFor = noVoteFor;
                stepDown = true;
            }


            //  check i am leader
            if (StringUtils.equals(RaftConstant.leader, this.state) && !stepDown) {
                electionResponse.setVoteGranted(false);
                electionResponse.setTerm(term);
                electionResponse.setReason("i am leader");

                return electionResponse;
            }


            // check already voted for you
            if (StringUtils.equals(candidateId, this.getId())) {
                electionResponse.setVoteGranted(true);
                electionResponse.setTerm(term);
                return electionResponse;
            }

            // check already voted for other peer
            if (!StringUtils.equals(noVoteFor, this.getId())) {
                electionResponse.setVoteGranted(false);
                electionResponse.setTerm(term);
                electionResponse.setReason("vote for other");
                return electionResponse;
            }


            // check match  last index and last term
            long lastIndex = this.logService.getLastIndex();
            long lastTerm = this.logService.getLastTerm();

            if (lastIndex > lastLogIndex || lastTerm > lastLogTerm) {
                electionResponse.setVoteGranted(false);
                electionResponse.setTerm(term);
                electionResponse.setReason(String.format("%s log.index %s > request.lastLogIndex %s or log.lastTerm %s > request.lastLogTerm %s", getId(), lastIndex, lastLogIndex, lastTerm, lastLogTerm));
                return electionResponse;
            }

            // set vote for the request candidate id
            this.voteFor = candidateId;
            electionResponse.setVoteGranted(false);
            electionResponse.setTerm(term);

            return electionResponse;

        } finally {
            this.lock.writeLock().unlock();
        }

    }

    /**
     * 并发复制
     */
    private void startConcurrentReplication() {

        this.lock.readLock().lock();
        log.info(String.format(">>>>>>>>>>%s concurrent replication log...<<<<<<<<<<", getId()));
        List<RaftPeer> recipients;
        try {
            recipients = this.configuration.getAllPeers().expect(getId()).explode();
        } finally {
            this.lock.readLock().unlock();
        }
        if (recipients == null || recipients.isEmpty()) {
            long lastIndex = this.logService.getLastIndex();
            if (lastIndex > 0) {
                log.info(String.format("%s commit to %s", getId(), lastIndex));
                this.logService.commitToIndex(lastIndex);
                log.info(String.format("%s commit to %s,commitIndex %s", getId(), lastIndex, this.logService.getLastCommittedIndex()));

            }

            return;
        }
        log.info(String.format("%s start concurrent replication log to other peers...", getId()));
        concurrentReplication(recipients);

    }

    private void concurrentReplication(List<RaftPeer> recipients) {


        for (final RaftPeer raftPeer : recipients) {

            long term = this.getTerm();
            long preLogIndex = this.nextIndexList.preLogIndex(raftPeer.getId());
            String leader = this.getLeader();
            List<RaftLog> raftLogs = this.getLogService().getRaftLogListFromIndex(preLogIndex);
            long committedIndex = this.getLogService().getLastCommittedIndex();

            final AppendEntriesRequest request = new AppendEntriesRequest();
            request.setLeaderId(leader);
            request.setCommitIndex(committedIndex);
            request.setEntries(raftLogs);
            request.setPreLogIndex(preLogIndex);
            request.setPreLogTerm(this.getLogService().getRaftLogTermBeginIndex(preLogIndex));
            request.setTerm(term);
            this.executorService.submit(new Runnable() {
                public void run() {
                    //发送请求
                    replicationService.replication(raftPeer, request, nextIndexList, new SimpleReplicationLogResponseHandler(request));
                }
            });
        }


    }

    /**
     * 重置选举超时定时器
     */
    public void resetElectionTimeoutTimer() {

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
    public void stopElectionTimeoutTimer() {
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

                startHeartbeat();

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
     * 重置并发复制日志定时器
     */
    private void resetReplicationScheduledTimer() {


        if (this.replicationScheduledFuture != null && !this.replicationScheduledFuture.isDone()) {
            this.replicationScheduledFuture.cancel(true);
        }

        // 初始化各个 peer index
        initNextIndex();

        this.replicationScheduledFuture = this.refreshScheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                startConcurrentReplication();
            }
        }, getBroadcastInterval(), getBroadcastInterval() * 2, TimeUnit.MILLISECONDS);

    }


    /**
     * 停止日志并发复制定时器
     */
    private void stopReplicationScheduledTimer() {

        if (this.replicationScheduledFuture != null && !this.replicationScheduledFuture.isDone()) {
            this.replicationScheduledFuture.cancel(true);
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


    /**
     * @notice 此方法只有在 peer 节点状态为 leader时候才能调用
     * 初始化 peer next index 集合
     */
    private void initNextIndex() {
        this.nextIndexList = new NextIndex(this.configuration.getAllPeers().expect(getId()).explode(), this.logService.getLastIndex());

    }

    /**
     * 广播时间间隔
     *
     * @return
     */
    private int getBroadcastInterval() {

        return RaftConstant.electionTimeoutMs / 10;
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
                        log.warn(String.format("ignore the election vote response request.term=%s,current.term=%s,election in the  %s  term  ", electionResponse.getTerm(), term, electionResponseTerm));
                        return;
                    }

                    //
                    if (term > electionResponse.getTerm()) {

                        log.warn(String.format("ignore the election vote response request.term=%s,current.term=%s,election int the  %s term <<<<<<<<<<", electionResponse.getTerm(), term, electionResponseTerm));
                        return;

                    }

                    //  有比自己高的任期号
                    if (electionResponseTerm > term) {

                        log.warn(String.format("found election vote response int the %s term  > the  current %s term <<<<<<<<<<", electionResponseTerm, term));
                        state = RaftConstant.follower;
                        term = electionResponse.getTerm();
                        leader = RaftConstant.noLeader;
                        voteFor = RaftConstant.noVoteFor;
                        log.info(String.format("raft:%s become %s in the %s term...<<<<<<<<<<", getId(), state, term));
                        return;
                    }
                    //  投票通过
                    if (voteGranted) {
                        log.info(String.format("%s vote to me...<<<<<<<<<<", raftPeer.getId()));
                        votes.putIfAbsent(tuple.getId(), true);
                    } else {
                        log.info(String.format("%s not vote  to me...<<<<<<<<<<", raftPeer.getId()));
                    }

                    //  获得大多数投票人的认可
                    if (configuration.pass(votes)) { // my win

                        log.info(String.format(">>>>>>>>>>%s I won the election in the %s term...<<<<<<<<<<", getId(), term));
                        this.becomeLeader();
                    }

                    // 没有通过继续等待下次选举的到来  continue

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
            log.info(String.format(">>>>>>>>>>>%s stop election timeout timer...<<<<<<<<<<", getId()));
            // 停止选举超时定时器
            stopElectionTimeoutTimer();
            log.info(String.format(">>>>>>>>>>>%s start send heartbeat schedule timer.....<<<<<<<<<<", getId()));
            resetHeartbeatTimer();

            log.info(String.format(">>>>>>>>>>>%s start concurrent replication log schedule timer .....<<<<<<<<<<", getId()));
            resetReplicationScheduledTimer();


        }
    }


    /**************************并发日志复制处理部分************************************************/
    /***
     * 并发复制响应处理器
     */
    private class SimpleReplicationLogResponseHandler implements ReplicationLogResponseHandler {

        private AppendEntriesRequest request;

        public SimpleReplicationLogResponseHandler(AppendEntriesRequest request) {
            this.request = request;
        }

        public void handler(RaftPeer peer, ReplicationLogResponseTuple tuple, NextIndex nextIndex) {
            log.info(String.format("%s replication response handler %s,response %s", getId(), peer.getId(), tuple));

            lock.writeLock().lock();

            try {
                long preLogIndex = request.getPreLogIndex();
                // check response
                if (request.getTerm() != term || !StringUtils.equals(RaftConstant.leader, state)) {
                    log.warn("ignore the replication log response  request.term %s not eq current.term %s or current.state is not leader", request.getTerm(), term, state);
                    return;
                }
                if (tuple.isSuccess()) {

                    AppendEntriesResponse appendEntriesResponse = tuple.getAppendEntriesResponse();
                    long responseTerm = appendEntriesResponse.getTerm();
                    if (responseTerm > term) {
                        log.info(String.format("response in the %s term > the current %s term ", responseTerm, term));
                        becomeFollower();
                    }


                    if (!appendEntriesResponse.isSuccess()) {
                        log.warn(String.format("%s replication log  to %s with pre log index %s fail", getId(), peer.getId(), preLogIndex));
                        nextIndex.decrement(peer.getId(), preLogIndex);
                    }

                    if (request.getEntries() != null && !request.getEntries().isEmpty()) {

                        if (startStatistic) {
                            statistics.putIfAbsent(peer.getId(), true);
                            if (configuration.pass(statistics)) {
                                long commitIndex = request.getEntries().get(request.getEntries().size() - 1).getIndex();
                                log.info(String.format("*************%s start raft log commit  with the %s index in %s term  **************", getId(), commitIndex, term));
                                // 提交本地日志
                                logService.commitToIndex(commitIndex);
                                // 通知成功
                                waitForDoneCondition.signalAll();
                            }


                        }

                        nextIndex.set(peer.getId(), logService.getLastIndex(), preLogIndex);
                    }


                }
                log.warn(String.format("%s replication log  to %s with pre log index %s not match,the next schedule again...", getId(), peer.getId(), preLogIndex));
                // continue

            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    /**
     * 成为跟随者
     */
    private void becomeFollower() {

        log.info(String.format("%s become follower in  the %s term", getId(), term));
        this.leader = noLeader;
        this.voteFor = noVoteFor;
        log.info(String.format("%s  stop heartbeat timer in the  %s term", getId(), term));
        this.stopHeartbeatTimer();
        log.info(String.format("%s  stop replication timer in the %s term", getId(), term));
        this.stopReplicationScheduledTimer();
        log.info(String.format("%s  reset election timeout timer in the %s term", getId(), term));
        this.resetElectionTimeoutTimer();
    }


    public String getId() {
        return id;
    }

    public String getState() {
        return state;
    }

    public long getTerm() {
        return term;
    }

    public RaftLogService getLogService() {
        return logService;
    }

    public void setLogService(RaftLogService logService) {
        this.logService = logService;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLeader() {
        return leader;
    }

    public String getVoteFor() {
        return voteFor;
    }
}
