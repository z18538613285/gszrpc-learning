package io.gushizhao.rpc.proxy.api;

import io.gushizhao.rpc.proxy.api.config.ProxyConfig;
import io.gushizhao.rpc.spi.annotation.SPI;

@SPI
public interface ProxyFactory {

    /**
     * 获取代理对象
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T getProxy(Class<T> clazz);

    /**
     * 默认初始化方法
     * @param proxyConfig
     * @param <T>
     */
    default <T> void init(ProxyConfig<T> proxyConfig){}
}
