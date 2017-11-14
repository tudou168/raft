package com.tongbanjie.raft.core.log.codec.impl;

import com.tongbanjie.raft.core.log.RaftLog;
import com.tongbanjie.raft.core.log.codec.RaftLogCodec;

import java.nio.ByteBuffer;

/***
 *
 * @author banxia
 * @date 2017-11-14 20:20:08
 */
public class Crc32RaftLogCodec implements RaftLogCodec {


    public byte[] encode(RaftLog raftLog) {
        return new byte[0];
    }

    public RaftLog decode(ByteBuffer buffer) {


        return null;
    }
}
