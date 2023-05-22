package io.gushizhao.rpc.codec;

import io.gushizhao.rpc.serialization.api.Serialization;
import io.gushizhao.rpc.serialization.jdk.JdkSerialization;
import io.gushizhao.rpc.spi.loader.ExtensionLoader;

public interface RpcCodec {


    /**
     * 根据 serializationType 通过 SPI 获取序列化句柄
     * @param serializationType
     * @return
     */
    default Serialization getSerialization(String serializationType) {
        return ExtensionLoader.getExtension(Serialization.class, serializationType);
    }
}
