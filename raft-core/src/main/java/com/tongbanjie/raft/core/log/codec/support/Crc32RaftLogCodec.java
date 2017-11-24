package com.tongbanjie.raft.core.log.codec.support;

import com.tongbanjie.raft.core.exception.RaftException;
import com.tongbanjie.raft.core.protocol.RaftLog;
import com.tongbanjie.raft.core.log.codec.RaftLogCodec;
import com.tongbanjie.raft.core.util.ByteUtil;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

/***
 * crc32
 * @author banxia
 * @date 2017-11-14 20:20:08
 */
public class Crc32RaftLogCodec implements RaftLogCodec {


    /***
     * 编码格式:
     *       -----------------------------------------------------
     *		| uint64 | uint64 | uint64 | uint32 | uint32 | []byte |
     *		 ------------------------------------------------------
     *		| CRC    | TERM   | INDEX  |   TYPE   | SIZE | CONTENT |
     *		 ------------------------------------------------------
     * @param raftLog raft 日志实体
     * @return
     */
    public byte[] encode(RaftLog raftLog) {

        byte[] content = raftLog.getContent();
        byte[] header = new byte[24];
        int offset = 0;

        // index
        ByteUtil.long2bytes(raftLog.getIndex(), header, offset);
        offset += 8;

        // term
        ByteUtil.long2bytes(raftLog.getIndex(), header, offset);
        offset += 8;

        //  log type
        ByteUtil.int2bytes(raftLog.getType(), header, offset);
        offset += 4;

        //  content length
        ByteUtil.int2bytes(content.length, header, offset);
        offset += 4;

        //body

        byte[] body = new byte[header.length + content.length];


        System.arraycopy(header, 0, body, 0, header.length);

        System.arraycopy(content, 0, body, header.length, content.length);


        CRC32 crc32 = new CRC32();
        crc32.update(body);

        long crc = crc32.getValue();

        byte[] result = new byte[body.length + 8];
        System.arraycopy(body, 0, result, 8, body.length);
        ByteUtil.long2bytes(crc, result, 0);
        return result;
    }

    /**
     * 解码
     * 格式:
     * -----------------------------------------------------
     * | uint64 | uint64 | uint64 | uint32 | uint32 | []byte |
     * ------------------------------------------------------
     * | CRC    | TERM   | INDEX  |   TYPE   | SIZE | CONTENT |
     * ------------------------------------------------------
     *
     * @param buffer
     * @return
     */
    public RaftLog decode(ByteBuffer buffer) {

        byte[] header = new byte[32];

        buffer.get(header);
        int offset = 0;
        // crc
        long crc = ByteUtil.bytes2long(header, offset);
        offset += 8;
        //  index
        long index = ByteUtil.bytes2long(header, offset);
        offset += 8;
        // term
        long term = ByteUtil.bytes2long(header, offset);
        offset += 8;
        // body_length
        int type = ByteUtil.bytes2int(header, offset);
        offset += 4;
        // body_length
        int length = ByteUtil.bytes2int(header, offset);
        offset += 4;

        if (buffer.remaining() < length) {
            throw new RaftException("decode  log data error: the body is not right");
        }

        //   content
        byte[] content = new byte[length];
        buffer.get(content);

        byte[] body = new byte[content.length + header.length - 8];

        System.arraycopy(header, 8, body, 0, header.length - 8);
        System.arraycopy(content, 0, body, header.length - 8, content.length);

        CRC32 crc32 = new CRC32();
        crc32.update(body);
        long value = crc32.getValue();

        if (value != crc) {
            throw new RaftException(String.format("index=%s,term=%s,crc=%s, sumCrc32=%s, check crc  error", index, term, crc, value));
        }

        return new RaftLog(index, term, type, content, null);

    }
}
