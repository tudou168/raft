package com.tongbanjie.raft.core.util;

import java.util.concurrent.atomic.AtomicLong;

/***
 *
 * @author banxia
 * @date 2017-11-21 11:11:39
 */
public class RequestIdGenerator {

    protected static final AtomicLong offset = new AtomicLong(0);
    protected static final int BITS = 20;
    protected static final long MAX_COUNT_PER_MILLIS = 1 << BITS;


    /**
     * 获取 requestId
     *
     * @return
     */
    public static long getRequestId() {
        long currentTime = System.currentTimeMillis();
        long count = offset.incrementAndGet();
        while (count >= MAX_COUNT_PER_MILLIS) {
            synchronized (RequestIdGenerator.class) {
                if (offset.get() >= MAX_COUNT_PER_MILLIS) {
                    offset.set(0);
                }
            }
            count = offset.incrementAndGet();
        }
        return (currentTime << BITS) + count;
    }
}
