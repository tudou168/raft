### Raft 项目


####    1.概述
***
1. 官网[raft](https://raft.github.io/)

2.  日志编码格式

```java

 /***
     * 编码格式:
     *       -----------------------------------------------------
     *		| uint64 | uint64 | uint64 | uint32 | uint32 | []byte |
     *		 ------------------------------------------------------
     *		| CRC    | TERM   | INDEX  |   TYPE   | SIZE | CONTENT |
     *		 ------------------------------------------------------
     * @param raftLog raft 日志实体
     * @return
     */

```

3. peer 生命周期

```java


//                                  times out,
//                                 new election
//     |                             .-----.
//     |                             |     |
//     v         times out,          |     v     receives votes from
// +----------+  starts election  +-----------+  majority of servers  +--------+
// | Follower |------------------>| Candidate |---------------------->| Leader |
// +----------+                   +-----------+                       +--------+
//     ^ ^                              |                                 |
//     | |    discovers current leader  |                                 |
//     | |                 or new term  |                                 |
//     | '------------------------------'                                 |
//     |                                                                  |
//     |                               discovers server with higher term  |
//     '------------------------------------------------------------------'
//
//

```
####    2.快速开始

1.  选定要启动的 peer 节点数并配置好指定的 peer 地址及端口号启动即可。

```java
package com.tongbanjie.raft.test.log.peer;

import com.tongbanjie.raft.core.bootstrap.RaftPeerBuilder;
import com.tongbanjie.raft.core.log.codec.support.Crc32RaftLogCodec;
import com.tongbanjie.raft.core.peer.RaftPeer;

/***
 *
 * @author banxia
 * @date 2017-11-28 15:15:12
 */
public class PeerBuilderTest {


    public static void main(String[] args) {

        String localServer = "127.0.0.1:6001";
        String servers = "127.0.0.1:6001,127.0.0.1:6002,127.0.0.1:6003";
        RaftPeerBuilder raftPeerBuilder = new RaftPeerBuilder();
        RaftPeer raftPeer = raftPeerBuilder
                .localServer(localServer)
                .servers(servers)
                .dataStoreFile("./" + localServer)
                .dataStoreFile("." + localServer + "_raft")
                .logCodec(new Crc32RaftLogCodec()).builder();
        raftPeer.bootstrap();
        while (true) ;
    }
}

```

2.  控制台输出

```text
[1128 17:00:23 442 DEBUG] [pool-1-thread-3] core.engine.RaftEngine - election vote response:{"electionResponse":null,"id":"127.0.0.1:6003","success":false}
[1128 17:00:23 456 DEBUG] [pool-1-thread-2] netty.util.Recycler - -Dio.netty.recycler.maxCapacityPerThread: 32768
[1128 17:00:23 456 DEBUG] [pool-1-thread-2] netty.util.Recycler - -Dio.netty.recycler.maxSharedCapacityFactor: 2
[1128 17:00:23 456 DEBUG] [pool-1-thread-2] netty.util.Recycler - -Dio.netty.recycler.linkCapacity: 16
[1128 17:00:23 457 DEBUG] [pool-1-thread-2] netty.util.Recycler - -Dio.netty.recycler.ratio: 8
[1128 17:00:23 489 DEBUG] [nioEventLoopGroup-2-5] netty.buffer.AbstractByteBuf - -Dio.netty.buffer.bytebuf.checkAccessible: true
[1128 17:00:23 493 DEBUG] [nioEventLoopGroup-2-5] netty.util.ResourceLeakDetectorFactory - Loaded default ResourceLeakDetector: io.netty.util.ResourceLeakDetector@c6853a8
[1128 17:00:23 949 DEBUG] [pool-1-thread-2] core.engine.RaftEngine - election vote response:{"electionResponse":{"term":4,"voteGranted":true,"reason":null},"id":"127.0.0.1:6002","success":true}
[1128 17:00:23 949 INFO ] [pool-1-thread-2] core.engine.RaftEngine - 127.0.0.1:6002 vote to me...<<<<<<<<<<
[1128 17:00:23 950 INFO ] [pool-1-thread-2] core.engine.RaftEngine - >>>>>>>>>>127.0.0.1:6001 I won the election in the 4 term...<<<<<<<<<<
[1128 17:00:23 950 DEBUG] [pool-1-thread-2] core.engine.RaftEngine - >>>>>>>>>>>127.0.0.1:6001 stop election timeout timer...<<<<<<<<<<
[1128 17:00:23 951 INFO ] [pool-1-thread-2] core.engine.RaftEngine - >>>>>>>>>>>127.0.0.1:6001 start send heartbeat schedule timer.....<<<<<<<<<<
[1128 17:00:23 953 INFO ] [pool-1-thread-2] core.engine.RaftEngine - >>>>>>>>>>>127.0.0.1:6001 start concurrent replication log schedule timer .....<<<<<<<<<<
[1128 17:00:24 458 DEBUG] [pool-3-thread-1] core.engine.RaftEngine - >>>>>>>>>>>127.0.0.1:6001 send heartbeat ...<<<<<<<<<<<
[1128 17:00:24 458 DEBUG] [pool-3-thread-1] core.engine.RaftEngine - 127.0.0.1:6001 into append log entry ...
[1128 17:00:24 460 DEBUG] [pool-3-thread-1] manage.support.DefaultRaftLogService - >>>>>>>>>>>>append raft log :{"index":1,"term":4,"content":[104,101,97,114,116,98,101,97,116],"type":1,"applyListener":null}
[1128 17:00:24 460 DEBUG] [pool-2-thread-1] core.engine.RaftEngine - >>>>>>>>>>127.0.0.1:6001 concurrent replication log...<<<<<<<<<<
[1128 17:00:24 460 DEBUG] [pool-2-thread-1] core.engine.RaftEngine - 127.0.0.1:6001 start concurrent replication log to other peers...
[1128 17:00:24 467 INFO ] [pool-1-thread-4] replication.support.DefaultReplicationService - 127.0.0.1:6002 send  append entries request {"term":4,"leaderId":"127.0.0.1:6001","preLogIndex":0,"preLogTerm":0,"entries":[{"index":1,"term":4,"content":[104,101,97,114,116,98,101,97,116],"type":1,"applyListener":null}],"commitIndex":0}
[1128 17:00:24 467 INFO ] [pool-1-thread-5] replication.support.DefaultReplicationService - 127.0.0.1:6003 send  append entries request {"term":4,"leaderId":"127.0.0.1:6001","preLogIndex":0,"preLogTerm":0,"entries":[{"index":1,"term":4,"content":[104,101,97,114,116,98,101,97,116],"type":1,"applyListener":null}],"commitIndex":0}
```

***

####    3.todolist

1.  ~~日志模块~~
2.  ~~配置管理模块~~
3.  ~~rpc通讯模块~~
4.  ~~选举模块~~
5.  ~~日志复制模块~~
6.  动态配置变更
7.  日志压缩
