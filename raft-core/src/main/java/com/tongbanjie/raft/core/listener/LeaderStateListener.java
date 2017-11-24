package com.tongbanjie.raft.core.listener;

/***
 *
 * leader state listener
 * @author banxia
 * @date 2017-11-24 14:14:32
 */
public interface LeaderStateListener {


    /**
     * listener notify
     *
     * @param id    leader id
     * @param state leader state
     */
    void notify(String id, Integer state);

}
