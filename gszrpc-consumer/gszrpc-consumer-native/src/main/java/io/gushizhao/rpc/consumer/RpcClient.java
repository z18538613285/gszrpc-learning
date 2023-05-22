package io.gushizhao.rpc.consumer;

import io.gushizhao.rpc.common.exception.RegistryException;
import io.gushizhao.rpc.consumer.common.RpcConsumer;
import io.gushizhao.rpc.proxy.api.ProxyFactory;
import io.gushizhao.rpc.proxy.api.async.IAsyncObjectProxy;
import io.gushizhao.rpc.proxy.api.config.ProxyConfig;
import io.gushizhao.rpc.proxy.api.object.ObjectProxy;
import io.gushizhao.rpc.proxy.jdk.JdkProxyFactory;
import io.gushizhao.rpc.registry.api.RegistryService;
import io.gushizhao.rpc.registry.api.config.RegistryConfig;
import io.gushizhao.rpc.registry.zookeeper.ZookeeperRegistryService;
import io.gushizhao.rpc.spi.loader.ExtensionLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/5 9:09
 */
public class RpcClient {

    private final Logger logger = LoggerFactory.getLogger(RpcClient.class);


    private String serviceVersion;
    private String serviceGroup;
    // 超时时间
    private long timeout = 15000;

    private RegistryService registryService;

    // 序列化类型
    private String serializationType;
    private boolean async;
    private boolean oneway;

    private int heartbeatInterval;
    private int scanNotActiveChannelInterval;

    private String proxy;

    // 重试间隔时间
    private int retryInterval = 1000;
    // 重试次数
    private int retryTimes = 3;

    public RpcClient(String registryAddress, String registryType, String registryLoadBalanceType, String proxy, String serviceVersion, String serviceGroup, long timeout,String serializationType, boolean async, boolean oneway, int heartbeatInterval, int scanNotActiveChannelInterval, int retryInterval, int retryTimes) {
        this.retryInterval = retryInterval;
        this.retryTimes = retryTimes;
        this.serviceVersion = serviceVersion;
        this.proxy = proxy;
        this.serviceGroup = serviceGroup;
        this.timeout = timeout;
        this.serializationType = serializationType;
        this.async = async;
        this.oneway = oneway;
        this.heartbeatInterval = heartbeatInterval;
        this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
        this.registryService = this.getRegistryService(registryAddress, registryType, registryLoadBalanceType);
    }

    public <T> T create(Class<T> interfaceClass) {
        ProxyFactory proxyFactory = ExtensionLoader.getExtension(ProxyFactory.class, proxy);
        proxyFactory.init(new ProxyConfig(interfaceClass, serviceVersion, serviceGroup, timeout, registryService, RpcConsumer.getInstance(heartbeatInterval, scanNotActiveChannelInterval, retryInterval, retryTimes), serializationType, async, oneway));
        return proxyFactory.getProxy(interfaceClass);
    }


    public <T> IAsyncObjectProxy createAsync(Class<T> interfaceClass) {
        return new ObjectProxy<T>(interfaceClass, serviceVersion, serviceGroup, timeout, registryService, RpcConsumer.getInstance(heartbeatInterval, scanNotActiveChannelInterval, retryInterval, retryTimes), serializationType, async, oneway);
    }


    private RegistryService getRegistryService(String registryAddress, String registryType, String registryLoadBalanceType) {
        if (StringUtils.isEmpty(registryType)) {
            throw new IllegalArgumentException("registry type is null");
        }

        // TODO 后续 SPI 扩展
        //RegistryService registryService = new ZookeeperRegistryService();
        RegistryService registryService = ExtensionLoader.getExtension(RegistryService.class, registryType);
        try {
            registryService.init(new RegistryConfig(registryAddress, registryType, registryLoadBalanceType));
        } catch (Exception e) {
            logger.error("RpcClient init registry service throws exception:{}", e);
            throw new RegistryException(e.getMessage(), e);
        }
        return registryService;
    }

    public void shutdown() {
        RpcConsumer.getInstance(heartbeatInterval, scanNotActiveChannelInterval, retryInterval, retryTimes).close();
    }
}
