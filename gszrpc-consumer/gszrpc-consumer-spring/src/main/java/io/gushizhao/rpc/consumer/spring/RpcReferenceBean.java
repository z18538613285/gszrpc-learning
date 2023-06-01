package io.gushizhao.rpc.consumer.spring;

import io.gushizhao.rpc.constants.RpcConstants;
import io.gushizhao.rpc.consumer.RpcClient;
import io.gushizhao.rpc.proxy.api.consumer.Consumer;
import io.gushizhao.rpc.registry.api.RegistryService;
import org.springframework.beans.factory.FactoryBean;

import javax.annotation.PostConstruct;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/19 16:16
 */
public class RpcReferenceBean implements FactoryBean<Object> {

    /**
     * 接口的 Class 对象
     */
    private Class<?> interfaceClass;

    private String version;

    private String registryType;

    private String loadBalanceType;

    private String serializationType;

    private String registryAddress;

    private String group;

    private long timeout;

    private boolean async;

    private boolean oneway;

    private String proxy;

    private Object object;

    private int scanNotActiveChannelInterval;

    private int heartbeatInterval;

    private int retryInterval = 1000;

    private int retryTimes = 3;

    private boolean enableResultCache;

    private int resultCacheExpire;

    // 是否开启直连服务
    private boolean enableDirectServer;
    // 直连服务的地址
    private String directServerUrl;

    // 是否开启延迟连接
    private boolean enableDelayConnection;

    private int corePoolSize;

    private int maximumPoolSize;

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

    private RpcClient rpcClient;

    @Override
    public Object getObject() throws Exception {
        return object;
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }


    public void init(){
       rpcClient = new RpcClient(registryAddress, registryType, loadBalanceType, proxy, version, group, timeout, serializationType,
               async, oneway, heartbeatInterval, scanNotActiveChannelInterval, retryInterval, retryTimes, enableResultCache,
               resultCacheExpire, enableDirectServer, directServerUrl, enableDelayConnection, corePoolSize, maximumPoolSize,
               flowType,enableBuffer, bufferSize, reflectType, fallbackClassName, enableRateLimiter, rateLimiterType, permits,
               milliSeconds, rateLimiterFailStrategy, enableFusing, fusingType, totalFailure, fusingMilliSeconds, exceptionPostProcessorType);
       rpcClient.setFallbackClass(fallbackClass);
       this.object = rpcClient.create(interfaceClass);
    }


    public RpcClient getRpcClient() {
        return rpcClient;
    }

    public String getExceptionPostProcessorType() {
        return exceptionPostProcessorType;
    }

    public void setExceptionPostProcessorType(String exceptionPostProcessorType) {
        this.exceptionPostProcessorType = exceptionPostProcessorType;
    }

    public boolean getEnableFusing() {
        return enableFusing;
    }

    public void setEnableFusing(boolean enableFusing) {
        this.enableFusing = enableFusing;
    }

    public String getFusingType() {
        return fusingType;
    }

    public void setFusingType(String fusingType) {
        this.fusingType = fusingType;
    }

    public double getTotalFailure() {
        return totalFailure;
    }

    public void setTotalFailure(double totalFailure) {
        this.totalFailure = totalFailure;
    }

    public int getFusingMilliSeconds() {
        return fusingMilliSeconds;
    }

    public void setFusingMilliSeconds(int fusingMilliSeconds) {
        this.fusingMilliSeconds = fusingMilliSeconds;
    }

    public String getRateLimiterFailStrategy() {
        return rateLimiterFailStrategy;
    }

    public void setRateLimiterFailStrategy(String rateLimiterFailStrategy) {
        this.rateLimiterFailStrategy = rateLimiterFailStrategy;
    }

    public boolean getEnableRateLimiter() {
        return enableRateLimiter;
    }

    public void setEnableRateLimiter(boolean enableRateLimiter) {
        this.enableRateLimiter = enableRateLimiter;
    }

    public String getRateLimiterType() {
        return rateLimiterType;
    }

    public void setRateLimiterType(String rateLimiterType) {
        this.rateLimiterType = rateLimiterType;
    }

    public int getPermits() {
        return permits;
    }

    public void setPermits(int permits) {
        this.permits = permits;
    }

    public int getMilliSeconds() {
        return milliSeconds;
    }

    public void setMilliSeconds(int milliSeconds) {
        this.milliSeconds = milliSeconds;
    }

    public String getReflectType() {
        return reflectType;
    }

    public void setReflectType(String reflectType) {
        this.reflectType = reflectType;
    }

    public String getFallbackClassName() {
        return fallbackClassName;
    }

    public void setFallbackClassName(String fallbackClassName) {
        this.fallbackClassName = fallbackClassName;
    }

    public Class<?> getFallbackClass() {
        return fallbackClass;
    }

    public void setFallbackClass(Class<?> fallbackClass) {
        this.fallbackClass = fallbackClass;
    }

    public boolean getEnableBuffer() {
        return enableBuffer;
    }

    public void setEnableBuffer(boolean enableBuffer) {
        this.enableBuffer = enableBuffer;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public String getFlowType() {
        return flowType;
    }

    public void setFlowType(String flowType) {
        this.flowType = flowType;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public boolean getEnableDelayConnection() {
        return enableDelayConnection;
    }

    public void setEnableDelayConnection(boolean enableDelayConnection) {
        this.enableDelayConnection = enableDelayConnection;
    }

    public boolean getEnableResultCache() {
        return enableResultCache;
    }

    public void setEnableResultCache(boolean enableResultCache) {
        this.enableResultCache = enableResultCache;
    }

    public int getResultCacheExpire() {
        return resultCacheExpire;
    }

    public void setResultCacheExpire(int resultCacheExpire) {
        this.resultCacheExpire = resultCacheExpire;
    }

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public void setInterfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRegistryType() {
        return registryType;
    }

    public void setRegistryType(String registryType) {
        this.registryType = registryType;
    }

    public String getLoadBalanceType() {
        return loadBalanceType;
    }

    public void setLoadBalanceType(String loadBalanceType) {
        this.loadBalanceType = loadBalanceType;
    }

    public String getSerializationType() {
        return serializationType;
    }

    public void setSerializationType(String serializationType) {
        this.serializationType = serializationType;
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public boolean isOneway() {
        return oneway;
    }

    public void setOneway(boolean oneway) {
        this.oneway = oneway;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public int getScanNotActiveChannelInterval() {
        return scanNotActiveChannelInterval;
    }

    public void setScanNotActiveChannelInterval(int scanNotActiveChannelInterval) {
        this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public int getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }


    public boolean getEnableDirectServer() {
        return enableDirectServer;
    }

    public void setEnableDirectServer(boolean enableDirectServer) {
        this.enableDirectServer = enableDirectServer;
    }

    public String getDirectServerUrl() {
        return directServerUrl;
    }

    public void setDirectServerUrl(String directServerUrl) {
        this.directServerUrl = directServerUrl;
    }
}
