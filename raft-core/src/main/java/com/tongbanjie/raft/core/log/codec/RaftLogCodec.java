package com.tongbanjie.raft.core.log.codec;

import com.tongbanjie.raft.core.protocol.RaftLog;

import java.nio.ByteBuffer;

/***
 * raft日志编码解码器
 * @author banxia
 * @date 2017-11-14 20:20:00
 */
public interface RaftLogCodec {


    /**
     * raft 日志编码
     *
     * @param raftLog raft 日志实体
     * @return
     */
    byte[] encode(RaftLog raftLog);

    /**
     * raft日志解码
     *
     * @param buffer
     * @return
     */
    RaftLog decode(ByteBuffer buffer);


}
