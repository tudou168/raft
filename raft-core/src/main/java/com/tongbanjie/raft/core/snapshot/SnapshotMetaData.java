package com.tongbanjie.raft.core.snapshot;

/***
 *
 * @author banxia
 * @date 2017-12-08 14:14:50
 */
public class SnapshotMetaData {

    private long lastIncludedIndex;

    private long lastIncludedTerm;

    private byte[] content;

    public long getLastIncludedIndex() {
        return lastIncludedIndex;
    }

    public void setLastIncludedIndex(long lastIncludedIndex) {
        this.lastIncludedIndex = lastIncludedIndex;
    }

    public long getLastIncludedTerm() {
        return lastIncludedTerm;
    }

    public void setLastIncludedTerm(long lastIncludedTerm) {
        this.lastIncludedTerm = lastIncludedTerm;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
