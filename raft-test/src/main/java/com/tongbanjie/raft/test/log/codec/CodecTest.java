package com.tongbanjie.raft.test.log.codec;

import com.tongbanjie.raft.core.protocol.RaftLog;
import com.tongbanjie.raft.core.log.codec.RaftLogCodec;
import com.tongbanjie.raft.core.log.codec.support.Crc32RaftLogCodec;
import com.tongbanjie.raft.core.log.store.DataStore;
import com.tongbanjie.raft.core.log.store.support.DefaultDataStore;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/***
 *
 * @author banxia
 * @date 2017-11-15 09:09:32
 */
public class CodecTest {

    private static final Logger log = LoggerFactory.getLogger(CodecTest.class);

    private RaftLogCodec raftLogCodec;
    private DataStore dataStore;

    @Before
    public void init() {

        this.raftLogCodec = new Crc32RaftLogCodec();
        this.dataStore = new DefaultDataStore("/Users/banxia/Desktop/wp", ".raft");
    }


    /***
     * 日志编码测试
     */
    @Test
    public void testEncode() {

        for (int i = 0; i < 100; i++) {

            RaftLog raftLog = new RaftLog();

            raftLog.setIndex(i);
            raftLog.setTerm(i + 1);
            raftLog.setContent(("内容:" + i).getBytes());
            log.info(String.format("准备编码 log:%s ...", raftLog));
            byte[] body = this.raftLogCodec.encode(raftLog);
            log.info(String.format("编码 log 完成..."));
            log.info(String.format("准备存储..."));
            if (this.dataStore.writeToStore(body)) {
                log.info("写入成功...");
            } else {
                log.info("写入失败...");
            }
        }
    }


    /***
     * 日志解码测试
     */
    @Test
    public void testDecode() {

        byte[] bytes = this.dataStore.readAll();
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        while (buffer.remaining() > 0) {

            RaftLog raftLog = this.raftLogCodec.decode(buffer);
            log.info("日志解码成功:" + raftLog);

        }
    }
}
