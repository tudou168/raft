package com.tongbanjie.raft.core.config;

/***
 *raft基础配置类
 * @author banxia
 * @date 2017-11-14 17:17:52
 */
public class RaftConfig {


    /**
     * 存储日志文件名
     */
    private String dataStorePath;

    /**
     * 存储日志文件目录
     */
    private String dataStoreFile;


    public String getDataStorePath() {
        return dataStorePath;
    }

    public void setDataStorePath(String dataStorePath) {
        this.dataStorePath = dataStorePath;
    }

    public String getDataStoreFile() {
        return dataStoreFile;
    }

    public void setDataStoreFile(String dataStoreFile) {
        this.dataStoreFile = dataStoreFile;
    }
}
