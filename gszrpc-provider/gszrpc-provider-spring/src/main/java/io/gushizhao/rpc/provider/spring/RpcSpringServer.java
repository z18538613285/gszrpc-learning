package io.gushizhao.rpc.provider.spring;

import io.gushizhao.rpc.annotation.RpcService;
import io.gushizhao.rpc.common.helper.RpcServiceHelper;
import io.gushizhao.rpc.constants.RpcConstants;
import io.gushizhao.rpc.protocol.meta.ServiceMeta;
import io.gushizhao.rpc.provider.common.server.base.BaseServer;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/19 14:38
 */
public class RpcSpringServer extends BaseServer implements ApplicationContextAware, InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(RpcSpringServer.class);

    public RpcSpringServer(String serverAddress, String registryAddress, String registryType, String registryLoadBalanceType,
                           String reflectType, int heartbeatInterval, int scanNotActiveChannelInterval, boolean enableResultCache,
                           int resultCacheExpire, int corePoolSize, int maximumPoolSize, String flowType, int maxConnections,
                           String disuseStrategyType, boolean enableBuffer, int bufferSize, boolean enableRateLimiter,
                           String rateLimiterType, int permits, int milliSeconds, String rateLimiterFailStrategy, boolean enableFusing,
                           String fusingType, double totalFailure, int fusingMilliSeconds, String exceptionPostProcessorType) {
        super(serverAddress, registryAddress, registryType, registryLoadBalanceType, reflectType, heartbeatInterval,
                scanNotActiveChannelInterval, enableResultCache, resultCacheExpire, corePoolSize, maximumPoolSize,
                flowType, maxConnections, disuseStrategyType, enableBuffer, bufferSize, enableRateLimiter,
                rateLimiterType, permits, milliSeconds, rateLimiterFailStrategy, enableFusing, fusingType, totalFailure,
                fusingMilliSeconds, exceptionPostProcessorType);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.startNettyServer();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        if (MapUtils.isNotEmpty(serviceBeanMap)) {
            for (Object serviceBean : serviceBeanMap.values()) {
                RpcService rpcService = serviceBean.getClass().getAnnotation(RpcService.class);
                ServiceMeta serviceMeta = new ServiceMeta(this.getServiceName(rpcService), rpcService.version(),host, port,  rpcService.group(), getWeight(rpcService.weight()));
                handlerMap.put(RpcServiceHelper.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion(), serviceMeta.getServiceGroup()), serviceBean);
                try {
                    registryService.register(serviceMeta);
                } catch (Exception e) {
                    logger.error("rpc server init spring exception{}", e);
                }
            }
        }
    }

    /**
     * 获取serviceName
     */
    private String getServiceName(RpcService rpcService){
        //优先使用interfaceClass
        Class clazz = rpcService.interfaceClass();
        if (clazz == null || clazz == void.class){
            return rpcService.interfaceClassName();
        }
        String serviceName = clazz.getName();
        if (serviceName == null || serviceName.trim().isEmpty()){
            serviceName = rpcService.interfaceClassName();
        }
        return serviceName;
    }

    /**
     * 为了将传入的 weight 属性范围控制在 1~100
     * @param weight
     * @return
     */
    private static int getWeight(int weight) {
        if (weight < RpcConstants.SERVICE_WEIGHT_MIN) {
            weight = RpcConstants.SERVICE_WEIGHT_MIN;
        }
        if (weight > RpcConstants.SERVICE_WEIGHT_MAX) {
            weight = RpcConstants.SERVICE_WEIGHT_MAX;
        }
        return weight;
    }
}
