package com.tongbanjie.raft.core.transport.netty;

import com.tongbanjie.raft.core.transport.Request;
import io.netty.channel.Channel;


/***
 *  request wrapper
 * @author banxia
 * @date 2017-12-02 18:18:14
 */
public class RequestWrapper {

    private Request request;
    private Channel channel;

    public RequestWrapper(Request request, Channel channel) {
        this.request = request;
        this.channel = channel;
    }

    public Request getRequest() {
        return request;
    }

    public Channel getChannel() {
        return channel;
    }
}
