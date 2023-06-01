package io.gushizhao.rpc.provider;

import io.gushizhao.rpc.provider.common.scanner.RpcServiceScanner;
import io.gushizhao.rpc.provider.common.server.base.BaseServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/4/23 14:03
 */
public class RpcSingleServer extends BaseServer {
    private final Logger logger = LoggerFactory.getLogger(RpcSingleServer.class);

    public RpcSingleServer(String serverAddress, String registryAddress, String registryType, String registryLoadBalanceType,
                           String scanPackage, String reflectType, int heartbeatInterval, int scanNotActiveChannelInterval,
                           boolean enableResultCache, int resultCacheExpire, int corePoolSize, int maximumPoolSize, String flowType,
                           int maxConnections, String disuseStrategyType, boolean enableBuffer, int bufferSize, boolean enableRateLimiter,
                           String rateLimiterType, int permits, int milliSeconds, String rateLimiterFailStrategy, boolean enableFusing,
                           String fusingType, double totalFailure, int fusingMilliSeconds, String exceptionPostProcessorType) {
        // 调用 父类构造方法
        super(serverAddress, registryAddress, registryType, registryLoadBalanceType, reflectType, heartbeatInterval,
                scanNotActiveChannelInterval, enableResultCache, resultCacheExpire, corePoolSize, maximumPoolSize,
                flowType, maxConnections, disuseStrategyType, enableBuffer, bufferSize, enableRateLimiter,
                rateLimiterType, permits, milliSeconds, rateLimiterFailStrategy, enableFusing,
                fusingType, totalFailure, fusingMilliSeconds, exceptionPostProcessorType);
        try {
            this.handlerMap = RpcServiceScanner.doScannerWithRpcServiceAnnotationFilterAndRegistryService(this.host, this.port, scanPackage, registryService);
        } catch (Exception e) {
            logger.error("RPC Server init error", e);
        }
    }

}
