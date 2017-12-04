package com.tongbanjie.raft.core.transport.netty.dispatcher;

import com.tongbanjie.raft.core.exception.TransportException;
import com.tongbanjie.raft.core.exception.TransportMethodNotFoundException;
import com.tongbanjie.raft.core.transport.Request;
import com.tongbanjie.raft.core.transport.Response;
import com.tongbanjie.raft.core.transport.netty.RequestWrapper;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/***
 *
 * @author banxia
 * @date 2017-12-02 18:18:17
 */
public class RequestHandlerDispatcher<T> {

    private final static Logger log = LoggerFactory.getLogger(RequestHandlerDispatcher.class);

    private T ref;

    private int threads;

    private ExecutorService executorService;

    private BlockingQueue<RequestWrapper> requestQueue = new LinkedBlockingQueue<RequestWrapper>();


    public RequestHandlerDispatcher(T ref, int threads) {
        this.ref = ref;
        this.threads = threads;
        this.init();
    }

    private void init() {

        this.executorService = Executors.newFixedThreadPool(threads);

        for (int a = 0; a < this.threads; a++) {

            this.executorService.submit(new RequestWorker<T>(this.ref, this.requestQueue));
        }


    }

    public void addRequestWrapper(RequestWrapper requestWrapper) {

        if (requestWrapper != null) {
            try {
                this.requestQueue.put(requestWrapper);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
    }


    private static class RequestWorker<T> implements Runnable {


        private T ref;


        private BlockingQueue<RequestWrapper> queue;

        public RequestWorker(T ref, BlockingQueue<RequestWrapper> queue) {
            this.ref = ref;
            this.queue = queue;
        }

        @Override
        public void run() {

            while (true) {
                RequestWrapper requestWrapper = null;

                try {

                    requestWrapper = this.queue.take();
                    Channel channel = requestWrapper.getChannel();
                    Request request = requestWrapper.getRequest();
                    String requestMethod = request.getMethod();
                    long requestId = request.getRequestId();
                    Object[] arguments = request.getArguments();
                    Class<?> serviceInterface = request.getServiceInterface();

                    Response response = new Response();
                    response.setRequestId(requestId);
                    boolean assignableFrom = serviceInterface.isAssignableFrom(ref.getClass());
                    if (!assignableFrom) {

                        log.error(String.format("%s is not assignableFrom %s", serviceInterface.getName(), this.ref.getClass().getName()));

                        response.setException(new TransportException(String.format("%s is not assignableFrom %s", serviceInterface.getName(), this.ref.getClass().getName())));
                        channel.writeAndFlush(response);
                        continue;
                    }


                    Class<?>[] parameterTypes = request.getParameterTypes();
                    Method method = serviceInterface.getMethod(requestMethod, parameterTypes);

                    if (method == null) {
                        response.setException(new TransportMethodNotFoundException(String.format("method %s not found", requestMethod)));
                    } else {

                        method.setAccessible(true);
                        Object result = method.invoke(ref, arguments);
                        response.setResult(result);
                    }

                    channel.writeAndFlush(response);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    if (null != requestWrapper) {
                        Response response = new Response();
                        response.setRequestId(requestWrapper.getRequest().getRequestId());
                        response.setException(e);
                        requestWrapper.getChannel().writeAndFlush(response);

                    }


                }
            }
        }
    }


}
