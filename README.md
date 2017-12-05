# Raft 项目
***

[raft 参数地址](https://raft.github.io/)

##    概述
***

>分布式一致性 **raft** 算法实现
    
    
##   角色
***

1.  Leader(领导)
    -   Leader(领导)在 raft 集群中同一个任期有且只有一个leader节点。
    -   Leader 节点主要维持整个 raft 集群中各个节点的状态、接收client的请求、状态、配置、以及数据以日志的形式同步到其他节点。
    -   Leader 定时发送心跳(心跳会当做日志) 到各个集群中的节点来阻止其他节点选举定时器超时。
2.  Candidate(候选人)
    -   Candidate(候选人)当 raft 集群中的选举定时器超时，当前节点会变成Candidate状态。
    -   只有拥有 Candidate 角色的才有能力发起投票选举请求。
    -   一个 raft 集群中有可能存在多个 Candidate 角色的节点。
    -   自己在发起投票请求前如果没有投票给其他节点则首先投一票给自己。
3.  Follower(跟随者)
    -   当 raft 节点启动时初始化为 Follower(跟随者)身份。
    -   Follower角色的节点只有接收处理 Leader 发送的日志或者选举投票请求。将日志追加到本地日志列表中。
    -   Follower角色的节点内部会维持一个随机投票选举超时定时器，当选举超时定时器超时后当前节点会变更为 Candidate 角色。

##  问题
***

*  投票选举
    *   相同的任期内每个节点仅仅能投一票。
    *   永远不投票给比自己本地保存日志的索引号小的投票请求。
    *   发现比自己本地保存的任期号大的投票选举请求则直接投票给它并更改为本地任期号为此请求的任期号，更改成功后直接将自己身份降级为跟随者。
    *   收到的投票数大于等于 N/2+1 个票数，赢得本届的选举直接变更为Leader身份。
    *   赢得选举开启发送到其他节点进行心跳、同步日志、接收客户端的请求。
    *   Leader 节点本地维护各个跟随者节点的最佳日志索引号列表(默认为当前 Leader 的最大的日志索引号)。
*   心跳
    *   Leader 发送心跳是为了阻止跟随者的随机超时投票定时器超时而触发选举请求。raft 使用简单的一个办法把心跳当做一个新的日志进行同步刷新到其他节点。
*   日志复制
    *   当前 Leader 接收到客户端的追加日志的请求，首先将日志追加到自己的本地日志列表中(注意:此追加的日志为未提交状态)。
    *   Leader 的同步刷新的日志定时器，会不断的进行复制日志到各个节点中。
    *   跟随者收到追加日志的请求，会根据提交的日志索引号来截断丢弃自己本地未提交的日志。如果提交请求的索引号小于自己本地提交的日志索引号将会拒绝截断操作。
    *   跟随者收到追加日志的请求，会将新的日志追加到自己的本地日志列表中(注意:日志还是未提交状态)。
    *   跟随者追加到自己本地日志列表中后，会根据自己已经提交的日志索引号与leader发起的日志列表进行比对，如果不匹配将拒绝请求。等待下次的到来。
    *   跟随者根据请求的参数leader提交的日志索引号，如果自己本地的提交的日志索引号小于提交请求过来的日志索引号将本地未提交的日志直接提交到指定的索引号。
    *   当 Leader 收到 大多数成功后会将本地的日志提交到对应的索引号。
    *   当前某个节点没有匹配成功会递减对应的 nextIndex 等待下次刷新同步的到来。


##   日志编码格式

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
##  Remoting Command 编码格式

```java

 /***
     * 编码格式:
     *       ------------------------------------------------------------
     *		| uint64       | uint32  | uint32       | uint32   |  []byte |
     *		 -------------------------------------------------------------
     *		| REQUESTID    | STATE   | COMMANDTYPE  |   SIZE   |  BODY   |
     *		 -------------------------------------------------------------
     * @param raftLog raft 日志实体
     * @return
     */


```

##  Peer 生命周期

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
##   快速开始

>   选定要启动的 **Peer** 节点数并配置好指定的peer地址及端口号启动即可。

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


3.  shell 方式启动

    1.  执行 ./install.sh 进行源码编译安装。
    2.  执行 ./raftServer.sh 127.0.0.1:6001 127.0.0.1:6001,127.0.0.1:6002,127.0.0.1:6003  ./log
    
4.  1.启动 raft 客户端
    ```shell
        ./raftCli.sh  -server 192.168.124.40:7001
    ```
    
##   todolist

1.  **~~日志模块~~**
2.  ~~配置管理模块~~
3.  ~~rpc通讯模块~~
4.  ~~选举模块~~
5.  ~~日志复制模块~~
6.  ~~日志恢复~~
7.  动态配置变更
8.  日志压缩
