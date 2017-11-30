package com.tongbanjie.raft.core.bootstrap;

import com.alibaba.fastjson.JSON;
import com.tongbanjie.raft.core.cmd.RaftCommand;
import com.tongbanjie.raft.core.constant.RaftConstant;
import com.tongbanjie.raft.core.enums.RaftCommandType;
import com.tongbanjie.raft.core.remoting.RemotingClient;
import com.tongbanjie.raft.core.remoting.RemotingCommand;
import com.tongbanjie.raft.core.remoting.builder.RemotingClientBuilder;
import com.tongbanjie.raft.core.util.NetUtil;
import com.tongbanjie.raft.core.util.RequestIdGenerator;
import org.apache.commons.lang.StringUtils;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

/***
 * raft client main class
 * @author banxia
 * @date 2017-11-30 15:15:44
 */
public class RaftClientMainBootstrap {


    public static void main(String[] args) {

        List<String> arguments = Arrays.asList(args);
        if (arguments == null || arguments.size() < 2) {
            System.out.println("argument has no enough");
            System.exit(1);
        }

        RemotingClient client = null;
        try {

            String command = arguments.get(0);
            String server = arguments.get(1);
            String[] ipp = server.split(":");
            String host = ipp[0];
            RaftCommand raftCommand = new RaftCommand();
            raftCommand.setName(command);
            if (StringUtils.equals(command, RaftConstant.join)) {
                raftCommand.setType(RaftCommandType.JOIN.getValue());
            } else if (StringUtils.equals(command, RaftConstant.leave)) {
                raftCommand.setType(RaftCommandType.LEAVE.getValue());
            } else {
                System.out.println("inval command !");
                System.exit(1);
            }

            InetAddress localAddress = NetUtil.getLocalAddress();
            String hostAddress = localAddress.getHostAddress();
            raftCommand.setConnectStr(hostAddress + ":6001");

            RemotingClientBuilder clientBuilder = new RemotingClientBuilder();
            client = clientBuilder.requestTimeout(5000).host(host).port(5001).builder();

            RemotingCommand remotingCommand = new RemotingCommand();
            remotingCommand.setRequestId(RequestIdGenerator.getRequestId());
            remotingCommand.setBody(JSON.toJSONString(raftCommand));

            RemotingCommand response = client.request(remotingCommand);

            System.out.println(response.getBody());


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("操作失败:" + e.getMessage());
            System.exit(1);
        } finally {
            if (client != null) {
                client.close();
            }
        }

    }

}
