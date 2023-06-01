package io.gushizhao.rpc.codec;

import io.gushizhao.rpc.processor.FlowPostProcessor;
import io.gushizhao.rpc.protocol.header.RpcHeader;
import io.gushizhao.rpc.serialization.api.Serialization;
import io.gushizhao.rpc.serialization.jdk.JdkSerialization;
import io.gushizhao.rpc.spi.loader.ExtensionLoader;
import io.gushizhao.rpc.threadpool.FlowPostProcessorThreadPool;

public interface RpcCodec {


    /**
     * 根据 serializationType 通过 SPI 获取序列化句柄
     * @param serializationType
     * @return
     */
    default Serialization getSerialization(String serializationType) {
        return ExtensionLoader.getExtension(Serialization.class, serializationType);
    }

    default void postFlowProcessor(FlowPostProcessor postProcessor, RpcHeader header) {
        // 异步调用流控分析后置处理器
        FlowPostProcessorThreadPool.submit(() -> {
            postProcessor.postRpcHeaderProcessor(header);
        });
    }
}
