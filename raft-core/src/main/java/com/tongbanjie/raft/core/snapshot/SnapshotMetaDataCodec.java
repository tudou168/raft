package com.tongbanjie.raft.core.snapshot;

import com.tongbanjie.raft.core.util.ByteUtil;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.CRC32;

/***
 *
 * @author banxia
 * @date 2017-12-08 15:15:46
 */
public class SnapshotMetaDataCodec {

    /**
     * 8        8                     8             4
     * crc32 + lastIncludeIndex + lastIncludeTerm  + size + content
     * encode the snapshot meta data
     *
     * @throws IOException
     */
    public void encode(RandomAccessFile randomAccessFile, SnapshotMetaData snapshotMetaData) throws IOException {

        long lastIncludedIndex = snapshotMetaData.getLastIncludedIndex();
        long lastIncludedTerm = snapshotMetaData.getLastIncludedTerm();
        byte[] content = snapshotMetaData.getContent();
        byte[] body = new byte[20 + content.length];
        int offset = 0;
        ByteUtil.long2bytes(lastIncludedIndex, body, offset);
        offset += 8;
        ByteUtil.long2bytes(lastIncludedTerm, body, offset);
        offset += 8;
        ByteUtil.int2bytes(content.length, body, offset);


        System.arraycopy(content, 0, body, 20, content.length);


        CRC32 crc32 = new CRC32();
        crc32.update(body);

        long crc = crc32.getValue();

        byte[] crcBytes = new byte[8];

        ByteUtil.long2bytes(crc, crcBytes, 0);

        byte[] result = new byte[28 + content.length];


        System.arraycopy(crcBytes, 0, result, 0, crcBytes.length);
        System.arraycopy(body, 0, result, crcBytes.length, body.length);

        randomAccessFile.write(result);

    }


    /**
     * decode
     */
    public SnapshotMetaData decode(RandomAccessFile randomAccessFile) throws IOException {

        if (randomAccessFile.length() < 28) {
            return null;
        }
        byte[] header = new byte[28];

        randomAccessFile.read(header);
        int offset = 0;
        long crc = ByteUtil.bytes2long(header, offset);
        offset += 8;
        long lastIncludedIndex = ByteUtil.bytes2long(header, offset);
        offset += 8;

        long lastIncludedTerm = ByteUtil.bytes2long(header, offset);
        offset += 8;

        int size = ByteUtil.bytes2int(header, offset);
        offset += 4;

        if (randomAccessFile.length() < size) {
            return null;
        }

        byte[] content = new byte[size];

        randomAccessFile.read(content);

        byte[] body = new byte[header.length + size - 8];

        System.arraycopy(header, 8, body, 0, header.length - 8);
        System.arraycopy(content, 0, body, header.length - 8, content.length);

        CRC32 crc32 = new CRC32();
        crc32.update(body);
        long value = crc32.getValue();
        if (value != crc) {

            return null;
        }

        SnapshotMetaData snapshotMetadata = new SnapshotMetaData();
        snapshotMetadata.setContent(content);
        snapshotMetadata.setLastIncludedIndex(lastIncludedIndex);
        snapshotMetadata.setLastIncludedTerm(lastIncludedTerm);
        return snapshotMetadata;
    }
}
