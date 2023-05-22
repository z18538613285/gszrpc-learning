package io.gushizhao.rpc.serialization.api;

import io.gushizhao.rpc.constants.RpcConstants;
import io.gushizhao.rpc.spi.annotation.SPI;

@SPI(RpcConstants.SERIALIZATION_JDK)
public interface Serialization {

    /**
     * 序列化
     * @param obj
     * @param <T>
     * @return
     */
    <T> byte[] serialize(T obj);

    /**
     * 反序列化
     * @param data
     * @param cls
     * @param <T>
     * @return
     */
    <T> T deserialize(byte[] data, Class<T> cls);
}
