package com.tongbanjie.raft.core.remoting.future;

import com.tongbanjie.raft.core.remoting.RemotingCommand;

/***
 *
 * @author banxia
 * @date 2017-11-21 17:17:13
 */
public interface RemotingResponseFuture {


    /**
     * 成功
     *
     * @param command
     */
    void onSuccess(RemotingCommand command);

    /**
     * 失败
     *
     * @param command
     */
    void onFail(RemotingCommand command);

    /**
     * 获取创建时间
     *
     * @return
     */
    long getCreateTime();

    /**
     * 获取响应指令
     *
     * @return
     */
    RemotingCommand getRemotingCommand();

    /**
     * 是否成功
     *
     * @return
     */
    boolean isSuccess();

    /**
     * 是否已取消
     *
     * @return
     */
    boolean isCanceled();

    /**
     * 是否完成
     *
     * @return
     */
    boolean isDone();


    /**
     * 取消等待响应
     */
    void cancel();


    void done();

    /**
     * 获取请求id
     *
     * @return
     */
    Long getRequestId();
}
