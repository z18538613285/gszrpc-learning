package io.gushizhao.rpc.registry.api;

import io.gushizhao.rpc.protocol.meta.ServiceMeta;
import io.gushizhao.rpc.registry.api.config.RegistryConfig;
import io.gushizhao.rpc.spi.annotation.SPI;

import java.io.IOException;

@SPI
public interface RegistryService {

    /**
     * 服务注册
     * @param serviceMeta 服务元数据
     * @throws Exception
     */
    void register(ServiceMeta serviceMeta) throws Exception;
    /**
     * 服务取消注册
     * @param serviceMeta 服务元数据
     * @throws Exception
     */
    void unRegister(ServiceMeta serviceMeta) throws Exception;

    /**
     * 服务发现
     * @param serviceName 服务名称
     * @param invokeHashCode HashCode 值
     * @return 服务元数据
     * @throws Exception 抛出异常
     */
    ServiceMeta discovery(String serviceName, int invokeHashCode, String sourceIp) throws Exception;

    /**
     * 服务销毁
     * @throws IOException
     */
    void destroy() throws IOException;

    /**
     * 默认初始化方法
     * @param registryConfig
     * @throws Exception
     */
    default void init(RegistryConfig registryConfig) throws Exception{}
}
