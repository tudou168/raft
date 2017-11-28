package com.tongbanjie.raft.core.constant;

/***
 *  raft 配置类
 * @author banxia
 * @date 2017-11-14 17:17:52
 */
public class RaftConstant {


    //  存储日志文件目录
    public final static String dataStorePath = ".";

    //  存储日志文件
    public final static String dataStoreFile = ".raft";

    //  默认端口
    public final static int defaultPort = 9876;


    //  raft engine state
    public final static String leader = "leader";

    public final static String candidate = "candidate";

    public final static String follower = "follower";


    //  leader status
    public final static String noLeader = "noLeader";

    public final static String noVoteFor = "noVoteFor";


    public final static int raftThreadNum = Runtime.getRuntime().availableProcessors() + 1;

    // 选举超时时间 最小为500毫秒
    public final static int electionTimeoutMs = 5000;

    //  心跳时间间隔
    public final static int heartbeatIntervalTimeMs = 500;


    public final static String logo = "\n" +
            "/***\n" +
            " *      _____        __ _      __               _                  \n" +
            " *     |  __ \\      / _| |    / _|             (_)                 \n" +
            " *     | |__) |__ _| |_| |_  | |_ ___  _ __     _  __ ___   ____ _ \n" +
            " *     |  _  // _` |  _| __| |  _/ _ \\| '__|   | |/ _` \\ \\ / / _` |\n" +
            " *     | | \\ \\ (_| | | | |_  | || (_) | |      | | (_| |\\ V / (_| |\n" +
            " *     |_|  \\_\\__,_|_|  \\__| |_| \\___/|_|      | |\\__,_| \\_/ \\__,_|\n" +
            " *                                            _/ |                 \n" +
            " *                                           |__/                  \n" +
            " */\n";


}
