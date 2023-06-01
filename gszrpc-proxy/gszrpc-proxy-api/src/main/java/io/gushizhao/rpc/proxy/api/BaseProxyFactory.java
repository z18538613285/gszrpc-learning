package io.gushizhao.rpc.proxy.api;

import io.gushizhao.rpc.proxy.api.config.ProxyConfig;
import io.gushizhao.rpc.proxy.api.object.ObjectProxy;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/8 14:46
 */
public abstract class BaseProxyFactory<T> implements ProxyFactory {

    protected ObjectProxy<T> objectProxy;

    @Override
    public <T> void init(ProxyConfig<T> proxyConfig) {
        this.objectProxy = new ObjectProxy(
                proxyConfig.getClazz(),
                proxyConfig.getServiceVersion(),
                proxyConfig.getServiceGroup(),
                proxyConfig.getTimeout(),
                proxyConfig.getRegistryService(),
                proxyConfig.getConsumer(),
                proxyConfig.getSerializationType(),
                proxyConfig.getAsync(),
                proxyConfig.getOneway(),
                proxyConfig.getEnableResultCache(),
                proxyConfig.getResultCacheExpire(),
                proxyConfig.getReflectType(),
                proxyConfig.getFallbackClassName(),
                proxyConfig.getFallbackClass(),
                proxyConfig.getEnableRateLimiter(),
                proxyConfig.getRateLimiterType(),
                proxyConfig.getPermits(),
                proxyConfig.getMilliSeconds(),
                proxyConfig.getRateLimiterFailStrategy(),
                proxyConfig.getEnableFusing(),
                proxyConfig.getFusingType(),
                proxyConfig.getTotalFailure(),
                proxyConfig.getFusingMilliSeconds(),
                proxyConfig.getExceptionPostProcessorType());
    }
}
