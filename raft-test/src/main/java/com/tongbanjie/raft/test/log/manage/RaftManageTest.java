package com.tongbanjie.raft.test.log.manage;

import com.tongbanjie.raft.core.log.manage.support.DefaultRaftLogManage;
import com.tongbanjie.raft.test.log.BaseTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        DefaultRaftLogManage raftLogManage = new DefaultRaftLogManage(this.dataStore, this.raftLogCodec);
        log.info(raftLogManage.getRaftLogList().toString());

    }
}
