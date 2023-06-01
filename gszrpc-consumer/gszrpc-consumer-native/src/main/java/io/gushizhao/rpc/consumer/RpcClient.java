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
import io.gushizhao.rpc.threadpool.ConcurrentThreadPool;
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

    private boolean enableResultCache;

    private int resultCacheExpire;

    // 是否开启直连服务
    private boolean enableDirectServer;
    // 直连服务的地址
    private String directServerUrl;

    // 是否开启延迟连接
    private boolean enableDelayConnection;

    private ConcurrentThreadPool concurrentThreadPool;

    private String flowType;

    private boolean enableBuffer;
    private int bufferSize;

    private String reflectType;

    private String fallbackClassName;

    private Class<?> fallbackClass;

    private boolean enableRateLimiter;

    private String rateLimiterType;

    private int permits;

    private int milliSeconds;

    private String rateLimiterFailStrategy;

    private boolean enableFusing;
    private String fusingType;
    private double totalFailure;
    private int fusingMilliSeconds;

    private String exceptionPostProcessorType;

    public RpcClient(String registryAddress, String registryType, String registryLoadBalanceType, String proxy, String serviceVersion,
                     String serviceGroup, long timeout,String serializationType, boolean async, boolean oneway, int heartbeatInterval,
                     int scanNotActiveChannelInterval, int retryInterval, int retryTimes, boolean enableResultCache, int resultCacheExpire,
                     boolean enableDirectServer, String directServerUrl, boolean enableDelayConnection, int corePoolSize, int maximumPoolSize,
                     String flowType, boolean enableBuffer, int bufferSize, String reflectType, String fallbackClassName,
                     boolean enableRateLimiter, String rateLimiterType, int permits, int milliSeconds, String rateLimiterFailStrategy,
                     boolean enableFusing, String fusingType, double totalFailure, int fusingMilliSeconds, String exceptionPostProcessorType) {
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
        this.enableResultCache = enableResultCache;
        this.resultCacheExpire = resultCacheExpire;
        this.enableDirectServer = enableDirectServer;
        this.directServerUrl = directServerUrl;
        this.enableDelayConnection = enableDelayConnection;
        this.concurrentThreadPool = ConcurrentThreadPool.getInstance(corePoolSize, maximumPoolSize);
        this.flowType = flowType;
        this.enableBuffer = enableBuffer;
        this.bufferSize = bufferSize;
        this.reflectType = reflectType;
        this.fallbackClassName = fallbackClassName;
        this.enableRateLimiter = enableRateLimiter;
        this.rateLimiterType = rateLimiterType;
        this.permits = permits;
        this.milliSeconds = milliSeconds;
        this.rateLimiterFailStrategy = rateLimiterFailStrategy;
        this.enableFusing = enableFusing;
        this.fusingType = fusingType;
        this.totalFailure = totalFailure;
        this.fusingMilliSeconds = fusingMilliSeconds;
        this.exceptionPostProcessorType = exceptionPostProcessorType;
    }

    public void setFallbackClass(Class<?> fallbackClass) {
        this.fallbackClass = fallbackClass;
    }

    public <T> T create(Class<T> interfaceClass) {
        ProxyFactory proxyFactory = ExtensionLoader.getExtension(ProxyFactory.class, proxy);
        proxyFactory.init(new ProxyConfig(interfaceClass, serviceVersion, serviceGroup, timeout, registryService,
                RpcConsumer.getInstance(heartbeatInterval, scanNotActiveChannelInterval, retryInterval, retryTimes)
                        .setEnableDirectServer(enableDirectServer)
                        .setDirectServerUrl(directServerUrl)
                        .setEnableDelayConnection(enableDelayConnection)
                        .setConcurrentThreadPool(concurrentThreadPool)
                        .setFlowPostProcessor(flowType)
                        .setEnableBuffer(enableBuffer)
                        .setBufferSize(bufferSize)
                        .buildNettyGroup()
                        .buildConnection(registryService),
                serializationType, async, oneway, enableResultCache, resultCacheExpire, reflectType, fallbackClassName, fallbackClass,
                enableRateLimiter, rateLimiterType, permits, milliSeconds, rateLimiterFailStrategy, enableFusing, fusingType, totalFailure,
                fusingMilliSeconds, exceptionPostProcessorType));
        return proxyFactory.getProxy(interfaceClass);
    }


    public <T> IAsyncObjectProxy createAsync(Class<T> interfaceClass) {
        return new ObjectProxy<T>(interfaceClass, serviceVersion, serviceGroup, timeout, registryService,
                RpcConsumer.getInstance(heartbeatInterval, scanNotActiveChannelInterval, retryInterval, retryTimes)
                        .setEnableDirectServer(enableDirectServer)
                        .setDirectServerUrl(directServerUrl)
                        .setEnableDelayConnection(enableDelayConnection)
                        .setConcurrentThreadPool(concurrentThreadPool)
                        .setFlowPostProcessor(flowType)
                        .setEnableBuffer(enableBuffer)
                        .setBufferSize(bufferSize)
                        .buildNettyGroup()
                        .buildConnection(registryService),
                serializationType, async, oneway, enableResultCache, resultCacheExpire, reflectType, fallbackClassName, fallbackClass,
                enableRateLimiter, rateLimiterType, permits, milliSeconds, rateLimiterFailStrategy, enableFusing, fusingType, totalFailure,
                fusingMilliSeconds, exceptionPostProcessorType);
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
