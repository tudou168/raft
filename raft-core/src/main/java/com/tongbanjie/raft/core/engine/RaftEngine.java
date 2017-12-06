package com.tongbanjie.raft.core.engine;

import com.alibaba.fastjson.JSON;
import com.tongbanjie.raft.core.builder.RaftPeerBuilder;
import com.tongbanjie.raft.core.cmd.RaftCommand;
import com.tongbanjie.raft.core.config.RaftConfiguration;
import com.tongbanjie.raft.core.constant.RaftConstant;
import com.tongbanjie.raft.core.election.RaftElectionService;
import com.tongbanjie.raft.core.election.handler.ElectionResponseHandler;
import com.tongbanjie.raft.core.election.support.DefaultRaftElectionService;
import com.tongbanjie.raft.core.enums.RaftCommandType;
import com.tongbanjie.raft.core.enums.RaftConfigurationState;
import com.tongbanjie.raft.core.enums.RaftLogType;
import com.tongbanjie.raft.core.exception.RaftException;
import com.tongbanjie.raft.core.listener.ConfigurationChangeListener;
import com.tongbanjie.raft.core.listener.LogApplyListener;
import com.tongbanjie.raft.core.log.manage.RaftLogService;
import com.tongbanjie.raft.core.peer.RaftPeer;
import com.tongbanjie.raft.core.peer.support.RaftPeerCluster;
import com.tongbanjie.raft.core.peer.support.RpcRaftPeer;
import com.tongbanjie.raft.core.protocol.*;
import com.tongbanjie.raft.core.replication.ReplicationService;
import com.tongbanjie.raft.core.replication.handler.ReplicationLogResponseHandler;
import com.tongbanjie.raft.core.replication.support.DefaultReplicationService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.tongbanjie.raft.core.constant.RaftConstant.follower;
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
    private RaftConfiguration config;

    //  raft 日志服务
    private RaftLogService logService;

    private RaftElectionService electionService;

    private ReplicationService replicationService;

    //  任务执行线程池
    private ExecutorService executorService;

    //  日志刷新调度线程
    private ScheduledExecutorService refreshScheduledExecutorService;


    // 任务调度线程池
    private ScheduledExecutorService scheduledExecutorService;

    //  选举超时调度器
    private ScheduledFuture electionTimeoutScheduledFuture;

    //  日志并发刷新调度器
    private ScheduledFuture replicationScheduledFuture;

    //  并发锁
    private ReadWriteLock lock = new ReentrantReadWriteLock();

    private Condition configCondition = lock.writeLock().newCondition();

    private SecureRandom random = new SecureRandom();

    //  用于保存各个 peer 刷新的索引号
    private NextIndex nextIndexList;

    //  投票列表
    private ConcurrentHashMap<String, Boolean> votes = new ConcurrentHashMap<String, Boolean>();


    private AtomicBoolean running = new AtomicBoolean(false);

    private long commitIndex;

    private ConcurrentHashMap<String, Long> matchIndexList = new ConcurrentHashMap<String, Long>();


    public RaftEngine(String id, RaftLogService logService) {
        this.id = id;
        this.logService = logService;
        this.electionService = new DefaultRaftElectionService();
        this.replicationService = new DefaultReplicationService();
        this.config = new RaftConfiguration();
        this.logService.setConfiguration(this.config);
        this.commitIndex = logService.getLastCommittedIndex();
    }


    public void setConfiguration(List<RaftPeer> peers, ConfigurationChangeListener changeListener) {

        if (peers == null || peers.isEmpty()) {
            throw new RaftException("peers is not allow null");
        }

        RaftPeerCluster cluster = new RaftPeerCluster();
        Map<String, RaftPeer> raftPeerMap = new HashMap<String, RaftPeer>();

        for (RaftPeer raftPeer : peers) {

            raftPeerMap.put(raftPeer.getId(), raftPeer);
        }
        cluster.setPeers(raftPeerMap);
        //  是否正在运行
        if (!this.running.get()) {
            this.config.directSetPeers(cluster);
            return;
        }
        // other
        this.changeConfiguration(cluster, changeListener);


    }

    /**
     * set new configuration
     *
     * @param cluster
     */
    private void changeConfiguration(final RaftPeerCluster cluster, final ConfigurationChangeListener changeListener) {

        this.lock.writeLock().lock();


        try {

            if (!StringUtils.equals(RaftConstant.leader, this.state)) {
                log.warn(String.format("%s is not leader !", getId()));
                return;
            }

            final List<String> peers = new ArrayList<String>();
            for (RaftPeer peer : cluster.explode()) {
                RaftPeer p = this.config.get(peer.getId());
                if (p != null) {
                    log.warn(String.format("the %s has already  in the configuration list", peer.getId()));
                    continue;
                }

                peers.add(peer.getId());
            }
            //  if has no new peer
            if (peers.isEmpty()) {
                changeListener.notify(this.config.getAllPeers().explode());
                return;
            }

            String peerStrs = JSON.toJSONString(peers);
            long lastIndex = this.logService.getLastIndex();
            lastIndex = lastIndex + 1;

            byte[] content = peerStrs.getBytes();
            RaftLog raftLog = new RaftLog(lastIndex, term, RaftLogType.CONFIGURATION.getValue(), content, new LogApplyListener() {

                public void notify(long commitIndex, RaftLog raftLog) {
                    changeListener.notify(cluster.explode());
                }
            });

            // append local configuration
            this.logService.appendRaftLog(raftLog);
            //  first append local configuration
            this.config.changeTo(cluster);

        } finally {
            this.lock.writeLock().unlock();
        }

    }


    /**
     * join the cluster
     *
     * @param raftCommand join cluster body
     * @return
     */
    public JoinResponse joinCluster(RaftCommand raftCommand) {

        this.lock.writeLock().lock();
        JoinResponse joinResponse = new JoinResponse();
        try {

            if (StringUtils.equals(this.config.getState(), RaftConfigurationState.CNEW.getName())) {
                joinResponse.setReason("the raft config is cOld,New state reject the operation!");
                return joinResponse;
            }


            if (!StringUtils.equals(RaftConstant.leader, this.state)) {
                log.warn(String.format("%s is not leader !", getId()));
                joinResponse.setReason("the request node state is not a leader state !");
                return joinResponse;
            }
            String connectStr = raftCommand.getConnectStr();
            //  check if exists
            boolean exists = this.config.containsPeer(connectStr);
            if (exists) { // has contains ?

                joinResponse.setSuccess(true);
                return joinResponse;
            }


            List<RaftPeer> peers = this.config.getOldPeers().explode();
            StringBuilder builder = new StringBuilder(connectStr);
            for (RaftPeer peer : peers) {

                builder.append(",").append(peer.getId());

            }

            String content = builder.toString();

            RaftPeerCluster peerCluster = new RaftPeerCluster();

            for (RaftPeer peer : peers) {
                peerCluster.getPeers().put(peer.getId(), peer);
            }

            RaftPeer p = new RpcRaftPeer(connectStr);
            p.registerRaftTransportClient();
            peerCluster.getPeers().put(connectStr, p);
            // append log

            long lastIndex = this.logService.getLastIndex();
            lastIndex = lastIndex + 1;
            final byte[] body = content.getBytes();
            RaftLog raftLog = new RaftLog(lastIndex, this.term, RaftLogType.CONFIGURATION.getValue(), body, new LogApplyListener() {
                @Override
                public void notify(long commitIndex, RaftLog raftLog) {
                    log.info(String.format("%s apply notify config...", getId()));
                    // TODO send again a cNew log to other
                    appendCNewLog(body);
                }
            });
            this.nextIndexList.getData().put(connectStr, lastIndex);
            // 追加到本地
            this.logService.appendRaftLog(raftLog);
            this.config.changeTo(peerCluster);

        } finally {
            this.lock.writeLock().unlock();
        }
        return joinResponse;
    }


    public LeaveResponse leaveCluster(RaftCommand raftCommand) {

        this.lock.writeLock().lock();
        LeaveResponse leaveResponse = new LeaveResponse();
        try {
            // check changing the peer config
            if (StringUtils.equals(this.config.getState(), RaftConfigurationState.CNEW.getName())) {
                leaveResponse.setReason("the raft config is cOld,New state reject the operation!");
                return leaveResponse;
            }

            //  check the raft peer is leader
            if (!StringUtils.equals(RaftConstant.leader, this.state)) {
                log.warn(String.format("%s is not leader !", getId()));
                leaveResponse.setReason("the request node state is not a leader  state !");
                return leaveResponse;
            }

            String connectStr = raftCommand.getConnectStr();
            //  check if exists
            boolean exists = this.config.containsPeer(connectStr);
            if (!exists) { // has not contains ?

                leaveResponse.setSuccess(true);
                return leaveResponse;
            }


            List<RaftPeer> peers = this.config.getOldPeers().explode();
            StringBuilder builder = new StringBuilder();
            for (RaftPeer peer : peers) {

                if (StringUtils.equals(peer.getId(), connectStr)) {
                    continue;
                } else {
                    builder.append(peer.getId()).append(",");
                }


            }
            String content = builder.toString();
            if (content.endsWith(",")) {
                content = content.substring(0, content.length() - 2);
            }

            log.info(">>>>>>>>>content=" + content + "<<<<<<<<<<<<<<<");

            RaftPeerCluster peerCluster = new RaftPeerCluster();

            for (RaftPeer peer : peers) {

                if (StringUtils.equals(peer.getId(), connectStr)) {
                    continue;
                }

                peerCluster.getPeers().put(peer.getId(), peer);
            }

            long lastIndex = this.logService.getLastIndex();
            lastIndex = lastIndex + 1;
            final byte[] body = content.getBytes();
            RaftLog raftLog = new RaftLog(lastIndex, this.term, RaftLogType.CONFIGURATION.getValue(), body, new LogApplyListener() {
                @Override
                public void notify(long commitIndex, RaftLog raftLog) {
                    log.info(String.format("%s apply notify config...", getId()));
                    // TODO send again a cNew log to other
                    appendCNewLog(body);
                }
            });
            // 追加到本地
            this.logService.appendRaftLog(raftLog);
            this.config.changeTo(peerCluster);


        } finally {

            this.lock.writeLock().unlock();
        }
        return leaveResponse;
    }

    /**
     * append cnew log
     *
     * @param body
     */
    private void appendCNewLog(final byte[] body) {

        this.lock.writeLock();

        try {
            long lastIndex = this.logService.getLastIndex();
            lastIndex = lastIndex + 1;
            RaftLog raftLog = new RaftLog(lastIndex, this.term, RaftLogType.CONFIGURATION_NEW.getValue(), body, new LogApplyListener() {
                @Override
                public void notify(long commitIndex, RaftLog raftLog) {
                    log.info(String.format("%s apply notify  cNew config...", getId()));

                    RaftPeer peer = config.getAllPeers().get(getId());
                    if (null == peer) {  //leave self
                        log.warn(String.format("%s self leave the raft cluster ", getId()));
                        //  leader become follower
                        becomeFollower();
                    }

                }
            });

            this.logService.appendRaftLog(raftLog);


        } finally {
            this.lock.writeLock().unlock();
        }
    }


    /***
     * 启动
     */
    public void bootstrap() {

        if (this.config.getAllPeers().size() == 0) {
            throw new RaftException("raft peers not allow null!");
        }
        initEngine();
        this.running.set(true);
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


    private boolean isOnlySelf() {

        List<RaftPeer> peers = this.config.getAllPeers().expect(getId()).explode();
        if (peers == null || peers.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * 开始选举
     */
    private void startElection() {

        this.lock.writeLock().lock();

        try {

            if (this.isOnlySelf()) {
                return;
            }

            //  the cluster is no me
            if (this.config.getAllPeers().get(getId()) == null) {

                return;
            }

            log.info(String.format("%s become candidate...", getId()));
            this.votes.clear();
            this.state = RaftConstant.candidate;
            this.voteFor = this.id;
            this.votes.put(this.id, true);
            this.leader = noLeader;
            this.term++;
            log.info(String.format("%s start Election with term=%s...", getId(), this.term));

        } finally {

            this.lock.writeLock().unlock();
        }


        List<RaftPeer> peers = this.config.getAllPeers().expect(this.id).explode();
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
        ElectionRequest electionRequest;
        try {
            if (this.isOnlySelf()) {
                return;
            }

            electionRequest = new ElectionRequest();
            long lastTerm = this.logService.getLastTerm();
            electionRequest.setLastLogTerm(lastTerm);
            electionRequest.setCandidateId(this.id);
            long lastIndex = this.logService.getLastIndex();
            electionRequest.setLastLogIndex(lastIndex);
            electionRequest.setTerm(term);
            log.debug(getId() + "***** lastTerm:--->" + lastTerm + ",lastIndex:--->" + lastIndex + "******>>>>electionRequest:" + electionRequest);
            log.debug(getId() + "********logs****" + this.logService.getRaftLogList());

        } finally {

            this.lock.readLock().unlock();
        }
        //  请求选举
        this.electionService.electionVoteRequest(peer, electionRequest, new SimpleElectionVoteResponseHandler(electionRequest));
    }

    /***
     * 追加日志
     *
     *
     */
    public void appendLogEntry(byte[] data, LogApplyListener applyListener) {


        this.lock.writeLock().lock();

        try {
            log.debug(String.format("%s into append log entry ...", getId()));

            if (!StringUtils.equals(RaftConstant.leader, this.state)) {
                log.warn(String.format("%s is not leader !", getId()));
                return;
            }
            long lastIndex = this.logService.getLastIndex();
            lastIndex = lastIndex + 1;
            RaftLog raftLog = new RaftLog(lastIndex, term, RaftLogType.DATA.getValue(), data, applyListener);
            raftLog.setApplyListener(applyListener);
            //  首先将追加到本地日志中
            this.logService.appendRaftLog(raftLog);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RaftException(e.getMessage(), e);
        } finally {
            this.lock.writeLock().unlock();
        }


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
        boolean stepDown = false;
        try {

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

            if (requestTerm > this.term) {

                log.warn(String.format("%s found request.term %s > current.term %s", getId(), requestTerm, this.term));
                this.term = requestTerm;
                this.leader = noLeader;
                this.voteFor = noVoteFor;
                this.state = RaftConstant.follower;
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
            if (!StringUtils.equals(noVoteFor, this.voteFor)) {
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

            //  rest the election timeout timer
            this.resetElectionTimeoutTimer();
            // set vote for the request candidate id
            this.voteFor = candidateId;
            electionResponse.setVoteGranted(true);
            electionResponse.setTerm(term);

            if (stepDown) {
                this.becomeFollower();
            }
            return electionResponse;

        } finally {

            if (stepDown) {
                becomeFollower();
            }
            this.lock.writeLock().unlock();
        }

    }


    /**
     * 追加日志请求处理
     *
     * @param request
     * @return
     */
    public AppendEntriesResponse appendEntriesHandler(AppendEntriesRequest request) {

        this.lock.writeLock().lock();

        boolean stepDown = false;
        long requestTerm = request.getTerm();
        long commitIndex = request.getCommitIndex();
        List<RaftLog> entries = request.getEntries();
        long preLogIndex = request.getPreLogIndex();
        long preLogTerm = request.getPreLogTerm();
        String leaderId = request.getLeaderId();
        try {

            AppendEntriesResponse appendEntriesResponse = new AppendEntriesResponse();
            // check term
            if (this.term > requestTerm) {

                appendEntriesResponse.setSuccess(false);
                appendEntriesResponse.setTerm(this.term);
                appendEntriesResponse.setReason(String.format("request.term %s <current.term %s", requestTerm, this.term));
                return appendEntriesResponse;

            }


            // check my term  is old term
            if (requestTerm > this.term) {
                this.term = requestTerm;
                this.leader = leaderId;
                this.voteFor = noVoteFor;
                stepDown = true;
            }

            if (requestTerm == this.term && !StringUtils.equals(this.leader, leaderId)) {
                this.term = requestTerm;
                this.leader = leaderId;
                this.voteFor = noVoteFor;
                stepDown = true;

            }

            // check i am waiting for the vote response
            if (StringUtils.equals(RaftConstant.candidate, this.state) && StringUtils.equals(this.leader, leaderId) && requestTerm > this.term) {
                this.term = requestTerm;
                this.leader = leaderId;
                stepDown = true;
            }
            // reset the election timeout timer
            this.resetElectionTimeoutTimer();

            //  截断本地
            boolean sec = this.logService.truncateRaftLog(preLogIndex, preLogTerm);
            if (!sec) {
                appendEntriesResponse.setSuccess(false);
                appendEntriesResponse.setTerm(this.term);
                appendEntriesResponse.setReason(String.format("preLogIndex %s or preLogTerm %s not match", preLogIndex, preLogTerm));
                return appendEntriesResponse;

            }

            this.resetElectionTimeoutTimer();

            //  将日志追加到本地
            for (RaftLog raftLog : entries) {
                boolean success = this.logService.appendRaftLog(raftLog);
                if (!success) {
                    log.warn(String.format("%s append log:%s fail", getId(), raftLog));
                    appendEntriesResponse.setSuccess(false);
                    appendEntriesResponse.setTerm(this.term);
                    appendEntriesResponse.setReason(String.format("append raft log fail with log:%s ", raftLog));
                    return appendEntriesResponse;

                } else {

                    if (raftLog.getType() == RaftLogType.CONFIGURATION.getValue() && raftLog.getContent() != null && raftLog.getContent().length > 0) {
                        String content = new String(raftLog.getContent());
                        //  TODO
                        log.info(String.format(">>>>>>>>>>%s start change the peer cluster config:%s <<<<<<<<<<<<<<<", getId(), content));
                        this.processConfigColdNewLog(content);

                    }

                }


//                else {
//
//
//                    if (raftLog.getType() == RaftLogType.CONFIGURATION.getValue() && raftLog.getContent() != null && raftLog.getContent().length > 0) {
//
//                        String content = new String(raftLog.getContent());
//
//                        if (content.startsWith(RaftConstant.join)) {
//
//                            this.processJoin(raftLog);
//
//                        } else if (content.startsWith(RaftConstant.leave)) {
//                            this.processLeave(raftLog);
//                        }
//
//
//                    }
//                    //  ignore
//                }
            }

            long lastCommittedIndex = this.logService.getLastCommittedIndex();

            if (commitIndex > 0 && commitIndex > lastCommittedIndex) {
                // commit the log
                this.logService.commitToIndex(commitIndex, false);
                log.info(String.format("%s raft log  committed to %s index", getId(), commitIndex));
            }

            // every good
            appendEntriesResponse.setSuccess(true);
            appendEntriesResponse.setTerm(this.term);
            appendEntriesResponse.setReason("append log success");
            return appendEntriesResponse;
        } finally {
            if (stepDown) {
                this.leader = leaderId;
                this.becomeFollower();
            }
            this.lock.writeLock().unlock();
        }
    }

    private void processConfigColdNewLog(String content) {


        String[] servers = content.split(",");

        RaftPeerCluster peerCluster = new RaftPeerCluster();
        for (String server : servers) {

            RaftPeer peer = this.config.get(server);
            if (null != peer) {
                peerCluster.getPeers().put(server, peer);
            } else {

                RaftPeer p = new RpcRaftPeer(server);
                p.registerRaftTransportClient();
                peerCluster.getPeers().put(server, p);
            }
        }
        this.config.changeTo(peerCluster);

    }

    /**
     * 并发复制
     */
    private void startConcurrentReplication() {

        this.lock.readLock().lock();
        log.debug(String.format(">>>>>>>>>>%s concurrent replication log...<<<<<<<<<<", getId()));
        List<RaftPeer> recipients;
        try {
            recipients = this.config.getAllPeers().expect(getId()).explode();
        } finally {
            this.lock.readLock().unlock();
        }
        if (recipients == null || recipients.isEmpty()) {
            long lastIndex = this.logService.getLastIndex();
            if (lastIndex > 0) {
                log.info(String.format("%s commit to %s", getId(), lastIndex));
                this.logService.commitToIndex(lastIndex, true);
                log.info(String.format("%s commit to %s,commitIndex %s", getId(), lastIndex, this.logService.getLastCommittedIndex()));

            }

            return;
        }
        log.debug(String.format("%s start concurrent replication log to other peers...", getId()));
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
        this.nextIndexList = new NextIndex(this.config.getAllPeers().expect(getId()).explode(), this.logService.getLastIndex());

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
            log.debug(String.format("election vote response:%s", tuple));

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
                        log.warn(String.format("raft:%s become %s in the %s term...<<<<<<<<<<", getId(), state, term));
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
                    if (config.pass(votes)) { // my win

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


            log.debug(String.format("%s replication response handler %s,response %s", getId(), peer.getId(), tuple));

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
                        long decrement = nextIndex.decrement(peer.getId(), preLogIndex);
                        if (decrement < 0) {
                            // reset the big
                            nextIndex.set(peer.getId(), logService.getLastIndex(), decrement);
                        }

                    }

                    if (request.getEntries() != null && !request.getEntries().isEmpty()) {

                        long newCommitIndex = request.getEntries().get(request.getEntries().size() - 1).getIndex();
                        //  set peer match index
                        config.get(peer.getId()).setMatchIndex(newCommitIndex);
                        nextIndex.set(peer.getId(), newCommitIndex, preLogIndex);
                        log.debug("peer.match.index=" + peer.getMatchIndex());
                        matchIndexList.put(peer.getId(), peer.getMatchIndex());
                        // start commit log
                        this.startCommitLog();
                    }


                }

                // continue

            } finally {

                lock.writeLock().unlock();
            }
        }

        /**
         *
         */
        private void startCommitLog() {


            matchIndexList.put(getId(), logService.getLastIndex());

            Collection<Long> values = matchIndexList.values();
            Long[] matchs = new Long[values.size()];
            values.toArray(matchs);

            Arrays.sort(matchs);
            long newCommitIndex = matchs[matchs.length / 2 - 1];
            long lastCommittedIndex = commitIndex;
            log.debug("***********lastCommittedIndex=" + lastCommittedIndex);
            if (newCommitIndex > lastCommittedIndex) {
                log.debug(String.format("*************%s start raft log commit  with the %s index in %s term  **************", getId(), newCommitIndex, term));
                logService.commitToIndex(newCommitIndex, true);
                commitIndex = newCommitIndex;

            }


        }
    }

    /**
     * 成为跟随者
     */
    private void becomeFollower() {

        log.info(String.format("%s become follower in  the %s term", getId(), term));
        this.voteFor = noVoteFor;
        this.state = follower;
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
