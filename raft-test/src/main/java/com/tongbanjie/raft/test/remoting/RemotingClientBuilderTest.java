package com.tongbanjie.raft.test.remoting;

import com.tongbanjie.raft.core.enums.RemotingCommandState;
import com.tongbanjie.raft.core.enums.RemotingCommandType;
import com.tongbanjie.raft.core.remoting.RemotingChannel;
import com.tongbanjie.raft.core.remoting.RemotingCommand;
import com.tongbanjie.raft.core.remoting.builder.RemotingClientBuilder;
import com.tongbanjie.raft.core.util.RequestIdGenerator;
import org.junit.Test;

/***
 *
 * @author banxia
 * @date 2017-11-28 11:11:07
 */
public class RemotingClientBuilderTest {

    @Test
    public void test() {

        RemotingChannel channel = new RemotingClientBuilder().host("127.0.0.1").port(9876).requestTimeout(3000).builder();


        while (true) {

            try {

                RemotingCommand command = new RemotingCommand();
                command.setRequestId(RequestIdGenerator.getRequestId());
                command.setState(RemotingCommandState.SUCCESS.getValue());
                command.setCommandType(RemotingCommandType.ELECTION.getValue());
                command.setBody("心跳");
                RemotingCommand response = channel.request(command);
                System.err.println("响应:" + response);
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
