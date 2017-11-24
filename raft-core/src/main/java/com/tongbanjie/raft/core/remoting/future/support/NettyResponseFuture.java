package com.tongbanjie.raft.core.remoting.future.support;

import com.tongbanjie.raft.core.enums.FutureState;
import com.tongbanjie.raft.core.remoting.RemotingCommand;
import com.tongbanjie.raft.core.remoting.future.RemotingResponseFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/***
 *  指令请求响应处理
 * @author banxia
 * @date 2017-11-21 17:17:15
 */
public class NettyResponseFuture implements RemotingResponseFuture {


    private final static Logger log = LoggerFactory.getLogger(NettyResponseFuture.class);

    private FutureState state = FutureState.DOING;

    private long createTime = System.currentTimeMillis();

    private Lock lock = new ReentrantLock();

    private Condition condition = lock.newCondition();

    private RemotingCommand response;

    private RemotingCommand request;

    private long timeout;

    /**
     * 是否成功
     */
    private boolean failed = false;


    public NettyResponseFuture(RemotingCommand request, long timeout) {
        this.request = request;
        this.timeout = timeout;
    }

    public void onSuccess(RemotingCommand command) {

        this.response = command;
        this.done();
    }

    public void onFail(RemotingCommand command) {

        this.response = command;
        this.failed = true;
        this.done();

    }

    public long getCreateTime() {
        return this.createTime;
    }

    /**
     * 阻塞获取响应指令结果
     *
     * @return
     */
    public RemotingCommand getRemotingCommand() {

        this.lock.lock();

        try {

            if (!state.isDoingState()) {
                return this.getValueOrThrowable();
            }

            if (this.timeout <= 0) {

                try {
                    this.condition.await(this.timeout, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    this.cancel();
                }
                return getValueOrThrowable();

            } else {

                long waitTime = this.timeout - (System.currentTimeMillis() - this.createTime);

                if (waitTime > 0) {

                    for (; ; ) {


                        try {
                            this.condition.await(waitTime, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException e) {

                        }

                        if (!this.state.isDoingState()) {
                            break;
                        }

                        waitTime = this.timeout - (System.currentTimeMillis() - this.createTime);

                        if (waitTime <= 0) {
                            break;
                        }
                    }

                }


                if (this.state.isDoingState()) {
                    this.cancel();
                }

            }


        } finally {
            this.lock.unlock();
        }
        return this.getValueOrThrowable();
    }

    private RemotingCommand getValueOrThrowable() {

        if (failed) {
            throw new RuntimeException("request remoting  fail");
        }
        return response;
    }

    public boolean isSuccess() {
        return isDone() && !this.failed;
    }

    public boolean isCanceled() {
        return state.isCanceledState();
    }

    public boolean isDone() {
        return state.isDoneState();
    }

    /**
     * 取消
     */
    public void cancel() {
        this.lock.lock();

        try {

            if (!state.isDoingState()) {
                return;
            }

            state = FutureState.CANCELED;
            this.failed = true;
            this.condition.signalAll();

        } finally {
            this.lock.unlock();
        }
    }

    /**
     * 完成
     */
    public void done() {
        this.lock.lock();

        try {

            if (!this.state.isDoingState()) return;
            this.state = FutureState.DONE;
            this.condition.signalAll();
        } finally {
            this.lock.unlock();
        }
    }

    public Long getRequestId() {

        return request.getRequestId();
    }
}
