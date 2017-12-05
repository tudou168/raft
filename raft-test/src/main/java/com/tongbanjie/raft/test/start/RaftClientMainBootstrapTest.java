package com.tongbanjie.raft.test.start;

import com.alibaba.fastjson.JSON;
import com.tongbanjie.raft.core.cmd.RaftCommand;
import com.tongbanjie.raft.core.constant.RaftConstant;
import com.tongbanjie.raft.core.enums.RaftCommandType;
import com.tongbanjie.raft.core.enums.RemotingCommandState;
import com.tongbanjie.raft.core.enums.RemotingCommandType;
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
public class RaftClientMainBootstrapTest {


    public static void main(String[] args) {

        args = new String[]{"raft:leave", "192.168.124.51"};
        List<String> arguments = Arrays.asList(args);
        if (arguments == null || arguments.size() < 2) {
            System.out.println("argument has no enough");
            System.exit(1);
        }


    }

}
