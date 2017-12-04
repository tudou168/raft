package com.tongbanjie.raft.core.transport.netty.serialization;

import java.io.IOException;

/***
 *
 * @author banxia
 * @date 2017-12-02 18:18:26
 */
public interface Serialization {

    byte[] serialize(Object obj) throws IOException;

    <T> T deserialize(byte[] bytes, Class<T> clz) throws IOException;

}
