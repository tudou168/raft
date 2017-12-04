package com.tongbanjie.raft.core.transport.netty.serialization.support;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.tongbanjie.raft.core.transport.netty.serialization.Serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/***
 *  BASE  Hessian2
 * @author banxia
 * @date 2017-12-02 18:18:28
 */
public class Hessian2Serialization implements Serialization {
    @Override
    public byte[] serialize(Object obj) throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Hessian2Output output = new Hessian2Output(outputStream);
        output.writeObject(obj);
        output.flush();
        return outputStream.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clz) throws IOException {

        Hessian2Input input = new Hessian2Input(new ByteArrayInputStream(bytes));
        return (T) input.readObject(clz);
    }
}
