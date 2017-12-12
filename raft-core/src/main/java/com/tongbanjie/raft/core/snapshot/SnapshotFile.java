package com.tongbanjie.raft.core.snapshot;

import java.io.RandomAccessFile;

/***
 *
 * @author banxia
 * @date 2017-12-08 14:14:43
 */
public class SnapshotFile {

    private String fileName;
    private RandomAccessFile randomAccessFile;

    public SnapshotFile(String fileName, RandomAccessFile randomAccessFile) {
        this.fileName = fileName;
        this.randomAccessFile = randomAccessFile;
    }

    public String getFileName() {
        return fileName;
    }

    public RandomAccessFile getRandomAccessFile() {
        return randomAccessFile;
    }
}
