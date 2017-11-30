package com.tongbanjie.raft.core.remoting.support.netty;

import com.alibaba.fastjson.JSON;
import com.tongbanjie.raft.core.cmd.RaftCommand;
import com.tongbanjie.raft.core.constant.RaftConstant;
import com.tongbanjie.raft.core.enums.RemotingCommandState;
import com.tongbanjie.raft.core.enums.RemotingCommandType;
import com.tongbanjie.raft.core.listener.LogApplyListener;
import com.tongbanjie.raft.core.peer.RaftPeer;
import com.tongbanjie.raft.core.protocol.*;
import com.tongbanjie.raft.core.remoting.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;
import sun.rmi.runtime.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/***
 *
 * @author banxia
 * @date 2017-11-22 15:15:49
 */
public class RemotingCommandProcessor {

    private RaftPeer peer;

    public RemotingCommandProcessor(RaftPeer peer) {
        this.peer = peer;
    }


    /**
     * 选举请求
     *
     * @param ctx
     * @param msg
     */
    public void electionVoteHandler(ChannelHandlerContext ctx, RemotingCommand msg) {


        RemotingCommand remotingCommand = new RemotingCommand();
        remotingCommand.setRequestId(msg.getRequestId());
        remotingCommand.setCommandType(RemotingCommandType.ELECTION.getValue());

        try {
            ElectionRequest electionRequest = JSON.parseObject(msg.getBody(), ElectionRequest.class);


            ElectionResponse electionResponse = this.peer.electionVoteHandler(electionRequest);
            remotingCommand.setBody(JSON.toJSONString(electionResponse));
            remotingCommand.setState(RemotingCommandState.SUCCESS.getValue());

        } catch (Exception e) {
            e.printStackTrace();
            remotingCommand.setState(RemotingCommandState.SUCCESS.getValue());
            remotingCommand.setBody("无效的数据");
        }

        ctx.writeAndFlush(remotingCommand);

    }

    /**
     * 追加日志
     *
     * @param ctx
     * @param msg
     */
    public void appendEntriesHandler(ChannelHandlerContext ctx, RemotingCommand msg) {


        String body = msg.getBody();
        AppendEntriesRequest appendEntriesRequest = JSON.parseObject(body, AppendEntriesRequest.class);


        AppendEntriesResponse appendEntriesResponse = this.peer.appendEntriesHandler(appendEntriesRequest);

        RemotingCommand remotingCommand = new RemotingCommand();
        remotingCommand.setRequestId(msg.getRequestId());
        remotingCommand.setState(RemotingCommandState.SUCCESS.getValue());
        remotingCommand.setBody(JSON.toJSONString(appendEntriesResponse));
        remotingCommand.setCommandType(RemotingCommandType.APPEND.getValue());
        ctx.writeAndFlush(remotingCommand);
    }

    /**
     * 执行命令
     * 如 raft:join 192.168.1.109:8081
     *
     * @param ctx
     * @param msg
     */
    public void commandHandler(ChannelHandlerContext ctx, RemotingCommand msg) {

        String body = msg.getBody();
        RaftCommand command = JSON.parseObject(body, RaftCommand.class);
        final BlockingQueue<Boolean> queue = new LinkedBlockingQueue<Boolean>();
        LogApplyListener listener = new LogApplyListener() {

            public void notify(long commitIndex, RaftLog raftLog) {
                try {
                    queue.put(true);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        this.peer.commandHandler(command, listener);

        RemotingCommand remotingCommand = new RemotingCommand();
        remotingCommand.setRequestId(msg.getRequestId());
        remotingCommand.setState(RemotingCommandState.SUCCESS.getValue());
        remotingCommand.setCommandType(RemotingCommandType.COMMAND.getValue());
        try {
            Boolean sec = queue.poll(3000, TimeUnit.MILLISECONDS);
            if (sec != null && sec) {
                remotingCommand.setBody("SUC");
            } else {
                remotingCommand.setBody("FAIL");
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
            remotingCommand.setBody("FAIL");
        }


        ctx.writeAndFlush(remotingCommand);


    }

}
