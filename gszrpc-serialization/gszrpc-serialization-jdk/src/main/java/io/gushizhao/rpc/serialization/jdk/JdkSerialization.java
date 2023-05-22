package io.gushizhao.rpc.serialization.jdk;

import io.gushizhao.rpc.common.exception.SerializerException;
import io.gushizhao.rpc.serialization.api.Serialization;
import io.gushizhao.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/4/23 15:51
 */
@SPIClass
public class JdkSerialization implements Serialization {

    private final Logger logger = LoggerFactory.getLogger(JdkSerialization.class);
    @Override
    public <T> byte[] serialize(T obj) {
        logger.info("execute jdk serialize...");
        if (obj == null) {
            throw new SerializerException("serializer object is null");
        }
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(os);
            out.writeObject(obj);
            return os.toByteArray();
        } catch (IOException e) {
            throw new SerializerException(e.getMessage(), e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> cls) {
        logger.info("execute jdk deserialize...");
        if (data == null) {
            throw new SerializerException("deserializer data is null");
        }
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(data);
            ObjectInputStream in = new ObjectInputStream(is);
            return (T)in.readObject();
        } catch (Exception e) {
            throw new SerializerException(e.getMessage(), e);
        }
    }
}
