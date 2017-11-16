package com.tongbanjie.raft.test.log.manage;

import com.tongbanjie.raft.core.protocol.RaftLog;
import com.tongbanjie.raft.test.log.BaseTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/***
 *
 * raft 日志管理测试类
 * @author banxia
 * @date 2017-11-15 10:10:49
 */
public class RaftManageTest extends BaseTest {

    private final static Logger log = LoggerFactory.getLogger(RaftManageTest.class);


    /**
     * 初始化
     */
    @Test
    public void initTest() {
        log.info(raftLogManage.getRaftLogList().toString());

        long index = 1;
        long term = 1;
        boolean contains = this.raftLogManage.contains(index, term);
        log.info(String.format(">>>>>>>>>>是否包含有 index:%s,term:%s,:%s", index, term, contains));

        // append wrong log
        log.info(">>>>>>>>>>>开始追加错误日志<<<<<<<<<<<<<<<<<<");
        RaftLog raftLog = new RaftLog(-1, 1, "错误的内容".getBytes());
        try {

            raftLogManage.appendRaftLog(raftLog.createCopy());
        } catch (Exception e) {
            log.error(">>>>>>>>>>追加日志失败:", e);
        }
        log.info(">>>>>>>>>>>开始追加正确日志<<<<<<<<<<<<<<<<<<");
        // append right  log
        appendRaftLog(raftLog);

        log.info(">>>>>>>>>>>开始获取 raft log 列表<<<<<<<<<<<<<<<<<<");
        // after raft  logs
        List<RaftLog> raftLogListFromIndex = getFromIndex();

        log.info(">>>>>>>>>>>开始截断 raft 日志<<<<<<<<<<<<<<<<<<");
        // truncate raft log
        truncateRaftLog(raftLogListFromIndex);


        commitRaftLog();


    }

    /***
     * 提交日志
     */
    private void commitRaftLog() {
        long lastIndex = this.raftLogManage.getLastIndex();
        log.info(">>>>>>>>>>>开始提交日志<<<<<<<<<<<<<<<<<<");
        long commitIndex = lastIndex - 1;
        this.raftLogManage.commitToIndex(commitIndex);
        log.info(String.format(">>>>>>>>>>>raft log commitIndex:%s, last index:%s", raftLogManage.getLastCommittedIndex(), raftLogManage.getLastIndex()));
    }

    /**
     * 追加日志
     *
     * @param raftLog
     */
    private void appendRaftLog(RaftLog raftLog) {

        raftLog.setIndex(raftLogManage.getLastIndex() + 1);
        raftLog.setTerm(raftLogManage.getLastTerm());
        raftLogManage.appendRaftLog(raftLog.createCopy());
        log.info(String.format(">>>>>>>>>>日志%s 追加成功", raftLog));

        raftLog.setIndex(raftLogManage.getLastIndex() + 1);
        raftLog.setTerm(raftLogManage.getLastTerm());
        raftLogManage.appendRaftLog(raftLog.createCopy());
        log.info(String.format(">>>>>>>>>>日志%s 追加成功", raftLog));

        raftLog.setIndex(raftLogManage.getLastIndex() + 1);
        raftLog.setTerm(raftLogManage.getLastTerm());
        raftLogManage.appendRaftLog(raftLog.createCopy());
        log.info(String.format(">>>>>>>>>>日志%s 追加成功", raftLog));

        raftLog.setIndex(raftLogManage.getLastIndex() + 1);
        raftLog.setTerm(raftLogManage.getLastTerm());
        raftLogManage.appendRaftLog(raftLog.createCopy());
        log.info(String.format(">>>>>>>>>>日志%s 追加成功", raftLog));


        raftLog.setIndex(raftLogManage.getLastIndex() + 1);
        raftLog.setTerm(raftLogManage.getLastTerm());
        raftLogManage.appendRaftLog(raftLog.createCopy());
        log.info(String.format(">>>>>>>>>>日志%s 追加成功", raftLog));

        raftLog.setIndex(raftLogManage.getLastIndex() + 1);
        raftLog.setTerm(raftLogManage.getLastTerm());
        raftLogManage.appendRaftLog(raftLog.createCopy());
        log.info(String.format(">>>>>>>>>>日志%s 追加成功", raftLog));


        raftLog.setIndex(raftLogManage.getLastIndex() + 1);
        raftLog.setTerm(raftLogManage.getLastTerm());
        raftLogManage.appendRaftLog(raftLog.createCopy());
        log.info(String.format(">>>>>>>>>>日志%s 追加成功", raftLog));

        raftLog.setIndex(raftLogManage.getLastIndex() + 1);
        raftLog.setTerm(raftLogManage.getLastTerm());
        raftLogManage.appendRaftLog(raftLog.createCopy());
        log.info(String.format(">>>>>>>>>>日志%s 追加成功", raftLog));

        raftLog.setIndex(raftLogManage.getLastIndex() + 1);
        raftLog.setTerm(raftLogManage.getLastTerm());
        raftLogManage.appendRaftLog(raftLog.createCopy());
        log.info(String.format(">>>>>>>>>>日志%s 追加成功", raftLog));


        raftLog.setIndex(raftLogManage.getLastIndex() + 1);
        raftLog.setTerm(raftLogManage.getLastTerm());
        raftLogManage.appendRaftLog(raftLog.createCopy());
        log.info(String.format(">>>>>>>>>>日志%s 追加成功", raftLog));

        raftLog.setIndex(raftLogManage.getLastIndex() + 1);
        raftLog.setTerm(raftLogManage.getLastTerm());
        raftLogManage.appendRaftLog(raftLog.createCopy());
        log.info(String.format(">>>>>>>>>>日志%s 追加成功", raftLog));
    }

    /**
     * 获取 日志
     *
     * @return
     */
    private List<RaftLog> getFromIndex() {
        long fromIndex = raftLogManage.getLastIndex() - 5;
        List<RaftLog> raftLogListFromIndex = raftLogManage.getRaftLogListFromIndex(fromIndex);
        log.info(String.format(">>>>>>>>>>fromIndex=%s, raft logs:%s", fromIndex, raftLogListFromIndex));
        return raftLogListFromIndex;
    }

    /**
     * 截断日志
     *
     * @param raftLogListFromIndex
     */
    private void truncateRaftLog(List<RaftLog> raftLogListFromIndex) {
        long idx = raftLogListFromIndex.get(0).getIndex();
        long tem = raftLogListFromIndex.get(0).getTerm();
        raftLogManage.truncateRaftLog(idx, tem);

        log.info(String.format(">>>>>>>>>>raft logs %s ,size %s", raftLogManage.getRaftLogList(), raftLogManage.getRaftLogList().size()));
    }


}
