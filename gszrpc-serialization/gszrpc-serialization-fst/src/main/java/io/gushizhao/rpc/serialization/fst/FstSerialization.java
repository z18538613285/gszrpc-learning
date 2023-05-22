package io.gushizhao.rpc.serialization.fst;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.gushizhao.rpc.common.exception.SerializerException;
import io.gushizhao.rpc.serialization.api.Serialization;
import io.gushizhao.rpc.spi.annotation.SPIClass;
import org.nustaq.serialization.FSTConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/11 16:26
 */
@SPIClass
public class FstSerialization implements Serialization {

    private final Logger logger = LoggerFactory.getLogger(FstSerialization.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public <T> byte[] serialize(T obj) {
        logger.info("execute fst serialize...");
        if (obj == null) {
            throw new SerializerException("serialize object is null");
        }
        FSTConfiguration conf = FSTConfiguration.getDefaultConfiguration();
        return conf.asByteArray(obj);
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> cls) {
        logger.info("execute fst deserialize...");
        if (data == null) {
            throw new SerializerException("serialize data is null");
        }
        FSTConfiguration conf = FSTConfiguration.getDefaultConfiguration();
        return (T) conf.asObject(data);
    }
}
