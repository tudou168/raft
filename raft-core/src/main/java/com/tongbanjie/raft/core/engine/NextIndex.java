package com.tongbanjie.raft.core.engine;

import com.tongbanjie.raft.core.exception.RaftException;
import com.tongbanjie.raft.core.peer.RaftPeer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/***
 *
 * @author banxia
 * @date 2017-11-19 10:10:26
 */
public class NextIndex {

    private Map<String, Long> data = new HashMap<String, Long>();

    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();


    public NextIndex(List<RaftPeer> peers, long defaultValue) {

        if (peers != null && !peers.isEmpty()) {


            for (RaftPeer peer : peers) {

                data.put(peer.getId(), defaultValue);
            }
        }
    }


    public long bestIndex() {

        readWriteLock.readLock().lock();

        try {

            if (data.isEmpty()) return 0;

            long i = Long.MAX_VALUE;
            for (Map.Entry<String, Long> entry : this.data.entrySet()) {

                Long nextIndex = entry.getValue();
                if (nextIndex < i) {
                    i = nextIndex;
                }
            }

            return i;

        } finally {
            readWriteLock.readLock().unlock();
        }
    }


    public long preLogIndex(String id) {

        this.readWriteLock.readLock().lock();

        try {

            Long index = this.data.get(id);
            if (index == null) {
                throw new RaftException(String.format("peer %s not found!", id));
            }

            return index;
        } finally {
            this.readWriteLock.readLock().unlock();
        }
    }


    public long decrement(String id, long pre) {

        this.readWriteLock.writeLock().lock();

        try {

            Long index = this.data.get(id);
            if (index == null) {
                throw new RaftException(String.format("peer %s not found!", id));
            }

            if (index != pre) {
                throw new RaftException(String.format("%s , index %s not eq pre %s", id, index, pre));
            }

            if (index > 0) {

                index--;
            }

            this.data.put(id, index);

            return index;

        } finally {
            this.readWriteLock.writeLock().unlock();
        }
    }


    public long set(String id, long index, long pre) {
        this.readWriteLock.writeLock().lock();

        try {

            Long nextIndex = this.data.get(id);
            if (nextIndex == null) {
                throw new RaftException(String.format("peer %s not found!", id));
            }

            if (nextIndex != pre) {
                throw new RaftException(String.format("%s , index %s not eq pre %s", id, index, pre));
            }
            nextIndex = index;
            this.data.put(id, nextIndex);
            return nextIndex;
        } finally {
            this.readWriteLock.writeLock().unlock();
        }

    }

}




