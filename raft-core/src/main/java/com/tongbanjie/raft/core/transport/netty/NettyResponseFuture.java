package com.tongbanjie.raft.core.transport.netty;

import com.tongbanjie.raft.core.exception.TransportException;
import com.tongbanjie.raft.core.exception.TransportTimeoutException;
import com.tongbanjie.raft.core.transport.FutureListener;
import com.tongbanjie.raft.core.transport.Request;
import com.tongbanjie.raft.core.transport.Response;
import com.tongbanjie.raft.core.transport.ResponseFuture;
import com.tongbanjie.raft.core.transport.enums.FutureState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/***
 *
 * @author banxia
 * @date 2017-12-04 11:11:28
 */
public class NettyResponseFuture implements ResponseFuture {

    private final static Logger log = LoggerFactory.getLogger(NettyChannel.class);

    private FutureState state = FutureState.DOING;

    private Exception exception;

    private long createTime = System.currentTimeMillis();

    private Request request;

    private long timeout;

    private Object result;

    private long processTime;

    private Lock lock = new ReentrantLock();

    private Condition condition = lock.newCondition();

    List<FutureListener> listeners;


    public NettyResponseFuture(Request request, long timeout) {
        this.request = request;
        this.timeout = timeout;
    }


    @Override
    public void onSuccess(Response response) {

        this.result = response.getResult();
        this.processTime = response.getProcessTime();
        this.done();

    }


    @Override
    public void onFailure(Response response) {

        this.exception = response.getException();
        this.processTime = response.getProcessTime();
        this.done();
    }

    @Override
    public long getCreateTime() {
        return this.createTime;
    }

    @Override
    public long getRequestId() {
        return this.request.getRequestId();
    }

    @Override
    public long getProcessTime() {
        return this.processTime;
    }

    @Override
    public boolean cancel() {

        TransportException transportException = new TransportException("nettyResponseFuture cancel the task cost=" + (System.currentTimeMillis() - this.createTime));

        return this.cancel(transportException);
    }

    private boolean cancel(TransportException exception) {

        this.lock.lock();

        try {
            if (!isDoing()) {
                return false;
            }

            this.state = FutureState.CANCELED;
            this.exception = exception;
            this.condition.signalAll();

        } finally {
            this.lock.unlock();
        }

        this.notifyListeners();
        return true;
    }


    private boolean isDoing() {

        return this.state.isDoingState();
    }

    @Override
    public boolean isCanceled() {
        return state.isCanceledState();
    }

    @Override
    public boolean isDone() {
        return state.isDoneState();
    }

    @Override
    public boolean isSuccess() {
        return isDone() && (null == exception);
    }

    /**
     * @return
     */
    @Override
    public Object getValue() {


        this.lock.lock();

        try {

            if (!isDoing()) {
                return getValueOrThrowable();
            }

            if (this.timeout <= 0) {

                try {
                    this.condition.await();
                } catch (InterruptedException e) {
                    log.error("netty response channel getValue interruptedException", e);
                }
                return this.getValueOrThrowable();
            } else {

                long waitTime = timeout - (System.currentTimeMillis() - this.createTime);

                if (waitTime > 0) {
                    for (; ; ) {

                        try {
                            this.condition.await(waitTime, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException e) {

                        }

                        if (!isDoing()) {
                            break;
                        }

                        waitTime = timeout - (System.currentTimeMillis() - this.createTime);
                        if (waitTime <= 0) {
                            break;
                        }

                    }
                }

                if (isDoing()) {
                    timeoutAndCancel();
                }

            }

        } finally {
            this.lock.unlock();
        }
        return getValueOrThrowable();
    }

    private void timeoutAndCancel() {

        this.processTime = System.currentTimeMillis() - this.createTime;
        this.lock.lock();

        try {

            if (!isDoing()) {
                return;
            }
            this.state = FutureState.CANCELED;
            this.exception = new TransportTimeoutException("NettyResponseFuture request timeout ,cost=" + processTime);
            this.condition.signalAll();

        } finally {
            this.lock.unlock();
        }

        this.notifyListeners();
    }


    private Object getValueOrThrowable() {

        if (this.exception != null) {

            throw (this.exception instanceof RuntimeException) ? ((RuntimeException) this.exception) : (new TransportException(this.exception.getMessage(), exception));
        }
        return this.result;
    }

    @Override
    public Exception getException() {

        return this.exception;
    }

    @Override
    public void addListener(FutureListener listener) {

        if (listener == null) {
            throw new TransportException("listener is not allow null");
        }
        boolean notify = false;

        this.lock.lock();

        try {

            if (!isDoing()) {
                notify = true;
            } else {
                if (this.listeners == null) this.listeners = new ArrayList<FutureListener>(1);
                this.listeners.add(listener);
            }

        } finally {
            this.lock.unlock();
        }

        if (notify) {

            this.notifyListener(listener);
        }
    }

    private void notifyListeners() {
        if (listeners != null) {
            for (FutureListener listener : listeners) {
                notifyListener(listener);
            }
        }
    }


    private void notifyListener(FutureListener listener) {

        try {

            listener.complete(this);

        } catch (Exception e) {

        }
    }


    private boolean done() {

        this.lock.lock();

        try {

            if (!isDoing()) {
                return false;
            }

            this.state = FutureState.DONE;
            this.condition.signalAll();

        } finally {
            this.lock.unlock();
        }

        this.notifyListeners();
        return true;
    }
}
