package io.gushizhao.rpc.provider.config;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/19 16:16
 */
public class SpringBootProviderConfig {

    private String serverAddress;

    private String registryAddress;

    private String registryType;

    private String registryLoadBalanceType;


    private String reflectType;

    private int scanNotActiveChannelInterval;

    private int heartbeatInterval;

    private boolean enableResultCache;

    private int resultCacheExpire;

    private int corePoolSize;

    private int maximumPoolSize;

    private String flowType;

    private int maxConnections;

    private String disuseStrategyType;

    private boolean enableBuffer;

    private int bufferSize;

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

    public SpringBootProviderConfig() {
    }

    public SpringBootProviderConfig(String serverAddress, String registryAddress, String registryType, String registryLoadBalanceType,
                                    String reflectType, int scanNotActiveChannelInterval, int heartbeatInterval, boolean enableResultCache,
                                    int resultCacheExpire, int corePoolSize, int maximumPoolSize, String flowType, int maxConnections,
                                    String disuseStrategyType, boolean enableBuffer, int bufferSize, boolean enableRateLimiter,
                                    String rateLimiterType, int permits, int milliSeconds, String rateLimiterFailStrategy, boolean enableFusing,
                                    String fusingType, double totalFailure, int fusingMilliSeconds, String exceptionPostProcessorType) {
        this.serverAddress = serverAddress;
        this.registryAddress = registryAddress;
        this.registryType = registryType;
        this.registryLoadBalanceType = registryLoadBalanceType;
        this.reflectType = reflectType;
        this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
        if (heartbeatInterval > 0) {
            this.heartbeatInterval = heartbeatInterval;
        }
        this.enableResultCache = enableResultCache;
        this.resultCacheExpire = resultCacheExpire;
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.flowType = flowType;
        this.maxConnections = maxConnections;
        this.disuseStrategyType = disuseStrategyType;
        this.enableBuffer = enableBuffer;
        this.bufferSize = bufferSize;
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

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public String getDisuseStrategyType() {
        return disuseStrategyType;
    }

    public void setDisuseStrategyType(String disuseStrategyType) {
        this.disuseStrategyType = disuseStrategyType;
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

    public boolean isEnableResultCache() {
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

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public String getRegistryType() {
        return registryType;
    }

    public void setRegistryType(String registryType) {
        this.registryType = registryType;
    }

    public String getRegistryLoadBalanceType() {
        return registryLoadBalanceType;
    }

    public void setRegistryLoadBalanceType(String registryLoadBalanceType) {
        this.registryLoadBalanceType = registryLoadBalanceType;
    }

    public String getReflectType() {
        return reflectType;
    }

    public void setReflectType(String reflectType) {
        this.reflectType = reflectType;
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
}
