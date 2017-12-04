package com.tongbanjie.raft.test.netty;

/***
 *
 * @author banxia
 * @date 2017-12-04 15:15:56
 */
public class EchoServiceImpl implements EchoService {
    public String echo(String name) {
        return "你好 " + name;
    }
}
