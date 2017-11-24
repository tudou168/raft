package com.tongbanjie.raft.core.log.manage.support;

import com.tongbanjie.raft.core.exception.RaftException;
import com.tongbanjie.raft.core.protocol.RaftLog;
import com.tongbanjie.raft.core.log.codec.RaftLogCodec;
import com.tongbanjie.raft.core.log.manage.RaftLogService;
import com.tongbanjie.raft.core.log.storage.DataStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/***
 *
 * raft 日志管理默认实现
 * @author banxia
 * @date 2017-11-15 10:10:37
 */
public class DefaultRaftLogService implements RaftLogService {

    private final static Logger log = LoggerFactory.getLogger(DefaultRaftLogService.class);

    private Lock lock = new ReentrantLock();
    //  数据存储
    private DataStorage dataStorage;

    //  raft 日志编码解码器
    private RaftLogCodec codec;

    //  raft 日志列表
    private List<RaftLog> raftLogs;

    private int committedPos = -1;


    public DefaultRaftLogService(DataStorage dataStorage, RaftLogCodec codec) {
        this.dataStorage = dataStorage;
        this.codec = codec;
        this.raftLogs = new ArrayList<RaftLog>();
        //  执行日志恢复
        this.recover();
    }

    /***
     *  执行 raft 日志恢复功能
     */
    private void recover() {

        byte[] bytes = this.dataStorage.readAll();
        if (bytes.length == 0) return;

        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes);
        buffer.flip();

        while (buffer.remaining() > 0) {

            try {

                //  解码
                RaftLog raftLog = this.codec.decode(buffer);
                //  追加日志
                this.appendRaftLog(raftLog);
                this.committedPos++;

            } catch (Exception e) {
                log.error(">>>>>>>raft 日志恢复失败>>>>>>>:", e);
                throw new RaftException("raft 日志恢复失败", e);
            }

        }

        log.info("raft 日志恢复成功...");

    }

    /**
     * 追加日志到日志列表中(未提交)
     *
     * @param raftLog raft 日志实体
     * @return
     */
    public boolean appendRaftLog(RaftLog raftLog) {

        this.lock.lock();

        try {

            if (this.raftLogs.isEmpty()) {
                this.raftLogs.add(raftLog);
            } else {

                long lastTerm = this.getLastTerm();
                if (raftLog.getTerm() < lastTerm) {
                    throw new RaftException("term too small error");
                }
                long lastIndex = this.getLastIndex();

                if (lastTerm == raftLog.getTerm() && raftLog.getIndex() <= lastIndex) {
                    throw new RaftException("index too small error");
                }

                this.raftLogs.add(raftLog);

            }


        } finally {
            this.lock.unlock();
        }
        return true;
    }

    /***
     * 给定 索引和任期 判断是否存在
     * @param index 日志索引
     * @param term  任期
     * @return
     */
    public boolean contains(long index, long term) {

        if (this.raftLogs.isEmpty()) return false;

        for (RaftLog raftLog : this.raftLogs) {

            if (raftLog.getIndex() == index && raftLog.getTerm() == term) {
                return true;
            }

            if (raftLog.getTerm() > term) {
                break;
            }
        }
        return false;
    }


    /**
     * 获取当前日志列表中的最后的索引
     *
     * @return
     */
    public long getLastIndex() {
        this.lock.lock();

        try {

            if (this.raftLogs.isEmpty()) return 0;

            return this.raftLogs.get(this.raftLogs.size() - 1).getIndex();

        } finally {

            this.lock.unlock();
        }

    }

    /**
     * 获取当前日志列表中的最后任期号
     *
     * @return
     */
    public long getLastTerm() {

        this.lock.lock();

        try {

            if (this.raftLogs.isEmpty()) return 0;

            return this.raftLogs.get(this.raftLogs.size() - 1).getTerm();

        } finally {

            this.lock.unlock();
        }

    }

    /**
     * 获取当前日志列表中已经提交存储的日志索引号
     *
     * @return
     */
    public long getLastCommittedIndex() {

        if (this.committedPos < 0) {
            return 0;
        }
        return this.raftLogs.get(this.committedPos).getIndex();
    }

    /**
     * 获取指定日志索引之后的所有日志列表
     *
     * @param index 日志索引
     * @return
     */
    public List<RaftLog> getRaftLogListFromIndex(long index) {

        this.lock.lock();

        try {

            int pos = 0;
            for (; pos < this.raftLogs.size(); pos++) {
                if (this.raftLogs.get(pos).getIndex() > index) {
                    break;
                }
            }


            if (pos > this.raftLogs.size()) {
                return new ArrayList<RaftLog>();
            }

            List<RaftLog> logs = new ArrayList<RaftLog>();
            for (; pos < this.raftLogs.size(); pos++) {
                logs.add(raftLogs.get(pos).createCopy());
            }
            return logs;

        } finally {
            this.lock.unlock();
        }

    }

    public long getRaftLogTermBeginIndex(long index) {
        this.lock.lock();

        try {

            long term = 0;
            int pos = 0;
            for (; pos < this.raftLogs.size(); pos++) {

                if (this.raftLogs.get(pos).getIndex() > index) {
                    break;
                }
                term = this.raftLogs.get(pos).getTerm();
            }

            return term;


        } finally {
            this.lock.unlock();
        }
    }

    /**
     * 给定日志的索引号和任期号进行切断操作
     *
     * @param index 日志索引号
     * @param term  任期号
     * @return
     */
    public boolean truncateRaftLog(long index, long term) {

        this.lock.lock();
        try {

            long lastCommittedIndex = this.getLastCommittedIndex();
            long lastIndex = this.getLastIndex();

            //  校验给定的索引范围
            if (index < lastCommittedIndex) {
                log.warn(String.format("index:%s < last commit index:%s error", index, lastCommittedIndex));
                return false;
            }

            if (index > lastIndex) {
                log.warn(String.format("index:%s > last index:%s error", index, lastIndex));
                return false;
            }

            //  如果给定的日志索引号为0 则将保存的日志列表进行初始化
            if (index == 0) {
                this.raftLogs = new CopyOnWriteArrayList<RaftLog>();
                return true;
            }

            int pos = 0;
            // 循环查找对应的日志的位置
            for (; pos < this.raftLogs.size(); pos++) {

                if (this.raftLogs.get(pos).getIndex() < index) {
                    continue;
                }

                if (this.raftLogs.get(pos).getIndex() > index) {
                    log.warn(String.format(" the index %s in log list not found error!", index));
                    return false;
                }

                if (this.raftLogs.get(pos).getIndex() != index) {
                    log.warn(String.format(" the index %s in log list not found error!", index));
                    return false;
                }

                if (term != this.raftLogs.get(pos).getTerm()) {
                    log.warn(String.format(" the term %s in log list not found error!", term));
                    return false;
                }
                // 正常退出循环
                break;
            }

            if (pos < this.committedPos) {
                log.warn(String.format("the pos:%s < committed pos:%s error", pos, committedPos));
                return false;
            }


            int truncateFrom = pos + 1;

            //  判断是否有要截取的日志内容
            if (truncateFrom >= this.raftLogs.size()) {
                return true;
            }
            this.raftLogs = this.raftLogs.subList(0, truncateFrom - 1);


        } finally {
            this.lock.unlock();
        }
        return false;
    }


    /**
     * 提交 raft 日志存储
     * 根据给定的日志索引号 将未提交的日志提交(存储到)对应的索引号的位置
     *
     * @param commitIndex 提交到指定的日志 index
     * @return
     */
    public boolean commitToIndex(long commitIndex) {

        this.lock.lock();

        try {

            long lastCommittedIndex = this.getLastCommittedIndex();
            // 如果当前已经提交的索引号不大于当前要提交的索引号
            if (commitIndex < lastCommittedIndex) {
                // ignore 忽略
                return true;
            }

            // 判断提交的索引号是否大于目前未提交日志的最大索引号
            long lastIndex = this.getLastIndex();
            if (commitIndex > lastIndex) {
                throw new RaftException(String.format("commit index:%s > last index:%s error!", commitIndex, lastCommittedIndex));
            }


            // 循环提交
            int pos = this.committedPos + 1;
            while (true) {

                if (pos >= this.raftLogs.size()) {
                    break;
                }
                if (this.raftLogs.get(pos).getIndex() > commitIndex) {
                    break;
                }

                //  写入存储 raft 日志
                byte[] body = this.codec.encode(this.raftLogs.get(pos));
                this.dataStorage.writeToStore(body);

                this.committedPos = pos;

                if (this.raftLogs.get(pos).getIndex() == commitIndex) {
                    break;
                }

                pos++;


            }

            return true;


        } finally {
            this.lock.unlock();
        }
    }


    /**
     * 获取raft日志列表
     *
     * @return
     */
    public List<RaftLog> getRaftLogList() {
        return this.raftLogs;
    }
}
