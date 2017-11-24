package com.tongbanjie.raft.core.log.storage;

/***
 *raft log 存储接口
 * @author banxia
 * @date 2017-11-14 17:17:43
 */
public interface DataStorage {


    /***
     * 从日志存储介质读取所有的日志数据
     * @return
     */
    byte[] readAll();

    /***
     * 将一条日志写入存储介质中
     * @param data
     * @return
     */
    boolean writeToStore(byte[] data);

}
