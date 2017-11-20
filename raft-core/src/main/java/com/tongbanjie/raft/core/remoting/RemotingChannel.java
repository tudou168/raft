package com.tongbanjie.raft.core.remoting;

/***
 *
 * @author banxia
 * @date 2017-11-20 21:21:20
 */
public interface RemotingChannel {


    /**
     * 打开
     *
     * @param host
     * @param port
     * @return
     */
    boolean open(String host, int port);

    /**
     * 关闭
     */
    void close();

    /**
     * 发送
     *
     * @param command
     * @return
     */
    RemotingCommand sendSync(RemotingCommand command);
}
