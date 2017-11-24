package com.tongbanjie.raft.test.log;

import com.tongbanjie.raft.core.log.codec.RaftLogCodec;
import com.tongbanjie.raft.core.log.codec.support.Crc32RaftLogCodec;
import com.tongbanjie.raft.core.log.manage.RaftLogService;
import com.tongbanjie.raft.core.log.manage.support.DefaultRaftLogService;
import com.tongbanjie.raft.core.log.storage.DataStorage;
import com.tongbanjie.raft.core.log.storage.support.DefaultDataStorage;
import org.junit.Before;

/***
 *
 * @author banxia
 * @date 2017-11-15 10:10:50
 */
public class BaseTest {

    protected RaftLogCodec raftLogCodec;
    protected DataStorage dataStorage;
    protected RaftLogService raftLogService;

    @Before
    public void init() {

        this.raftLogCodec = new Crc32RaftLogCodec();
        this.dataStorage = new DefaultDataStorage("/Users/banxia/Desktop/wp", ".raft");
        raftLogService = new DefaultRaftLogService(this.dataStorage, this.raftLogCodec);
    }
}
