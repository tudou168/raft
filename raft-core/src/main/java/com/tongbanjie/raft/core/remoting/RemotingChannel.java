package com.tongbanjie.raft.core.remoting;

/***
 *
 * @author banxia
 * @date 2017-11-21 14:14:30
 */
public interface RemotingChannel {

    /**
     * 打开
     *
     * @return
     */
    boolean open();


    /**
     * 关闭
     */

    void close();

    /**
     * 是否关闭
     * @return
     */
    boolean isClosed();


    boolean isAvailable();

    /**
     * 同步请求
     * @param command
     * @return
     */
    RemotingCommand request(RemotingCommand command);



}
