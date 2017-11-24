package com.tongbanjie.raft.core.listener;

import com.tongbanjie.raft.core.peer.RaftPeer;

import java.util.List;

/***
 *
 * @author banxia
 * @date 2017-11-24 15:15:07
 */
public interface ConfigurationChangeListener {

    void notify(List<RaftPeer> peers);
}
