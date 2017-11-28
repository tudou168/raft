package com.tongbanjie.raft.remoting.test;

import com.tongbanjie.raft.core.remoting.RemotingChannel;
import com.tongbanjie.raft.core.remoting.builder.RemotingServerBuilder;
import com.tongbanjie.raft.core.remoting.support.netty.RemotingCommandProcessor;
import org.junit.Test;

/***
 *
 * @author banxia
 * @date 2017-11-28 11:11:11
 */
public class RemotingServerBuilderTest {

    @Test
    public void test() {

        RemotingChannel channel = new RemotingServerBuilder().host("127.0.0.1").port(9876).remotingCommandProcessor(new RemotingCommandProcessor(null)).builder();

        while (true) ;
    }
}
