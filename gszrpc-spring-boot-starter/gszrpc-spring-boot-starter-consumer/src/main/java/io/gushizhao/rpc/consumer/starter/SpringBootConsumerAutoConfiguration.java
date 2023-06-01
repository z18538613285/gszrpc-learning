package io.gushizhao.rpc.consumer.starter;

import io.gushizhao.rpc.constants.RpcConstants;
import io.gushizhao.rpc.consumer.RpcClient;
import io.gushizhao.rpc.consumer.config.SpringBootConsumerConfig;
import io.gushizhao.rpc.consumer.spring.RpcReferenceBean;
import io.gushizhao.rpc.consumer.spring.context.RpcConsumerSpringContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/22 14:21
 */
@Configuration
public class SpringBootConsumerAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "gszrpc.gushizhao.consumer")
    public SpringBootConsumerConfig springBootConsumerConfig() {
        return new SpringBootConsumerConfig();
    }


    /**
     * fix06
     * 在服务消费者的核心注解@RpcReference的设计中，可以支持服务消费者基于@RpcReference注解同时连接多个不同的服务提供者实例。但是在实际测试过程中，
     * 发现基于SpringBoot启动服务消费者时，无法同时连接多个服务提供者。
     *
     * rpcClient()方法通过@Bean注解向Spring容器中注入一个RpcClient对象。此时，无论@RpcReference注解中同时连接了多少个服务提供者，
     * 但是真正运行代码后，服务消费者只会与一个服务提供者建立连接。
     *
     * 解决
     * 向Spring容器中注入一个RpcClient对象集合，集合中的每个RpcClient对象对应一个RpcReferenceBean对象，对应一个@RpcReference对象。
     *
     * @param springBootConsumerConfig
     * @return
     */
    @Bean
    public List<RpcClient> rpcClient(final SpringBootConsumerConfig springBootConsumerConfig){
        return parseRpcClient(springBootConsumerConfig);
    }

    private List<RpcClient> parseRpcClient(final SpringBootConsumerConfig springBootConsumerConfig){
        List<RpcClient> rpcClientList = new ArrayList<>();
        ApplicationContext context = RpcConsumerSpringContext.getInstance().getContext();
        Map<String, RpcReferenceBean> rpcReferenceBeanMap = context.getBeansOfType(RpcReferenceBean.class);
        Collection<RpcReferenceBean> rpcReferenceBeans = rpcReferenceBeanMap.values();
        for (RpcReferenceBean rpcReferenceBean : rpcReferenceBeans){
            rpcReferenceBean = this.getRpcReferenceBean(rpcReferenceBean, springBootConsumerConfig);
            rpcReferenceBean.init();
            rpcClientList.add(rpcReferenceBean.getRpcClient());
        }
        return rpcClientList;
    }


    /**
     *
     * fix01：
     * 在执行的过程中，SpringBootConsumerAutoConfiguration类下的rpcClient()方法会在RpcConsumerPostProcessor类的postProcessBeanFactory()方法之后运行，
     * 就会导致服务消费者服务yml文件中的值覆盖掉解析的@RpcReference注解中的值。
     *
     * 解决：
     * 在bhrpc-consumer-spring工程下的io.binghe.rpc.consumer.spring.RpcConsumerPostProcessor#setApplicationContext()方法中
     * 保存ApplicationContext对象，在SpringBootConsumerAutoConfiguration类中解析yml文件时，优先读取ApplicationContext对象中存储的RpcReferenceBean对象
     * 中的值，如果RpcReferenceBean对象中的值为null或者RpcReferenceBean对象中的值是@RpcReference注解的默认值，并且yml文件中配置了对应的值，
     * 则再使用yml文件中的值覆盖掉解析的@RpcReference注解的值。
     *
     * @param springBootConsumerConfig
     * @return
     */
   /* @Bean
    public RpcClient rpcSpringServer(final SpringBootConsumerConfig springBootConsumerConfig) {
        return new RpcClient(springBootConsumerConfig.getRegistryAddress(),
                springBootConsumerConfig.getRegistryType(),
                springBootConsumerConfig.getLoadBalanceType(),
                springBootConsumerConfig.getProxy(),
                springBootConsumerConfig.getVersion(),
                springBootConsumerConfig.getGroup(),
                springBootConsumerConfig.getTimeout(),
                springBootConsumerConfig.getSerializationType(),
                springBootConsumerConfig.getAsync(),
                springBootConsumerConfig.getOneway(),
                springBootConsumerConfig.getHeartbeatInterval(),
                springBootConsumerConfig.getScanNotActiveChannelInterval(),
                springBootConsumerConfig.getRetryInterval(),
                springBootConsumerConfig.getRetryTimes(),
                springBootConsumerConfig.getEnableResultCache(),
                springBootConsumerConfig.getResultCacheExpire(),
                springBootConsumerConfig.getEnableDirectServer(),
                springBootConsumerConfig.getDirectServerUrl(),
                springBootConsumerConfig.getEnableDelayConnection(),
                springBootConsumerConfig.getCorePoolSize(),
                springBootConsumerConfig.getMaximumPoolSize(),
                springBootConsumerConfig.getFlowType(),
                springBootConsumerConfig.getEnableBuffer(),
                springBootConsumerConfig.getBufferSize(),
                springBootConsumerConfig.getReflectType(),
                springBootConsumerConfig.getFallbackClassName(),
                springBootConsumerConfig.getEnableRateLimiter(),
                springBootConsumerConfig.getRateLimiterType(),
                springBootConsumerConfig.getPermits(),
                springBootConsumerConfig.getMilliSeconds(),
                springBootConsumerConfig.getRateLimiterFailStrategy(),
                springBootConsumerConfig.getEnableFusing(),
                springBootConsumerConfig.getFusingType(),
                springBootConsumerConfig.getTotalFailure(),
                springBootConsumerConfig.getFusingMilliSeconds(),
                springBootConsumerConfig.getExceptionPostProcessorType());
    }

*/
    /**
     * 首先从Spring IOC容器中获取RpcReferenceBean，
     * 如果存在RpcReferenceBean，部分RpcReferenceBean的字段为空，则使用springBootConsumerConfig字段进行填充
     * 如果不存在RpcReferenceBean，则使用springBootConsumerConfig构建RpcReferenceBean
     */
    private RpcReferenceBean getRpcReferenceBean(final RpcReferenceBean referenceBean, final SpringBootConsumerConfig springBootConsumerConfig){
        if (StringUtils.isEmpty(referenceBean.getGroup())
                || (RpcConstants.RPC_COMMON_DEFAULT_GROUP.equals(referenceBean.getGroup()) && !StringUtils.isEmpty(springBootConsumerConfig.getGroup()))){
            referenceBean.setGroup(springBootConsumerConfig.getGroup());
        }
        if (StringUtils.isEmpty(referenceBean.getVersion())
                || (RpcConstants.RPC_COMMON_DEFAULT_VERSION.equals(referenceBean.getVersion()) && !StringUtils.isEmpty(springBootConsumerConfig.getVersion()))){
            referenceBean.setVersion(springBootConsumerConfig.getVersion());
        }
        if (StringUtils.isEmpty(referenceBean.getRegistryType())
                || (RpcConstants.RPC_REFERENCE_DEFAULT_REGISTRYTYPE.equals(referenceBean.getRegistryType()) && !StringUtils.isEmpty(springBootConsumerConfig.getRegistryType()))){
            referenceBean.setRegistryType(springBootConsumerConfig.getRegistryType());
        }
        if (StringUtils.isEmpty(referenceBean.getLoadBalanceType())
                || (RpcConstants.RPC_REFERENCE_DEFAULT_LOADBALANCETYPE.equals(referenceBean.getLoadBalanceType()) && !StringUtils.isEmpty(springBootConsumerConfig.getLoadBalanceType()))){
            referenceBean.setLoadBalanceType(springBootConsumerConfig.getLoadBalanceType());
        }
        if (StringUtils.isEmpty(referenceBean.getSerializationType())
                || (RpcConstants.RPC_REFERENCE_DEFAULT_SERIALIZATIONTYPE.equals(referenceBean.getSerializationType()) && !StringUtils.isEmpty(springBootConsumerConfig.getSerializationType()))){
            referenceBean.setSerializationType(springBootConsumerConfig.getSerializationType());
        }
        if (StringUtils.isEmpty(referenceBean.getRegistryAddress())
                || (RpcConstants.RPC_REFERENCE_DEFAULT_REGISTRYADDRESS.equals(referenceBean.getRegistryAddress()) && !StringUtils.isEmpty(springBootConsumerConfig.getRegistryAddress()))){
            referenceBean.setRegistryAddress(springBootConsumerConfig.getRegistryAddress());
        }
        if (referenceBean.getTimeout() <= 0
                || (RpcConstants.RPC_REFERENCE_DEFAULT_TIMEOUT == referenceBean.getTimeout() && springBootConsumerConfig.getTimeout() > 0)){
            referenceBean.setTimeout(springBootConsumerConfig.getTimeout());
        }
        if (!referenceBean.isAsync()){
            referenceBean.setAsync(springBootConsumerConfig().getAsync());
        }
        if (!referenceBean.isOneway()){
            referenceBean.setOneway(springBootConsumerConfig().getOneway());
        }
        if (StringUtils.isEmpty(referenceBean.getProxy())
                || (RpcConstants.RPC_REFERENCE_DEFAULT_PROXY.equals(referenceBean.getProxy()) && !StringUtils.isEmpty(springBootConsumerConfig.getProxy()) )){
            referenceBean.setProxy(springBootConsumerConfig.getProxy());
        }
        if (referenceBean.getHeartbeatInterval() <= 0
                || (RpcConstants.RPC_COMMON_DEFAULT_HEARTBEATINTERVAL == referenceBean.getHeartbeatInterval() && springBootConsumerConfig.getHeartbeatInterval() > 0 )){
            referenceBean.setHeartbeatInterval(springBootConsumerConfig.getHeartbeatInterval());
        }
        if (referenceBean.getRetryInterval() <= 0
                || (RpcConstants.RPC_REFERENCE_DEFAULT_RETRYINTERVAL == referenceBean.getRetryInterval() && springBootConsumerConfig.getRetryInterval() > 0)){
            referenceBean.setRetryInterval(springBootConsumerConfig.getRetryInterval());
        }
        if (referenceBean.getRetryTimes() <= 0
                || (RpcConstants.RPC_REFERENCE_DEFAULT_RETRYTIMES == referenceBean.getRetryTimes() && springBootConsumerConfig.getRetryTimes() > 0)){
            referenceBean.setRetryTimes(springBootConsumerConfig.getRetryTimes());
        }
        if (referenceBean.getScanNotActiveChannelInterval() <= 0
                || (RpcConstants.RPC_COMMON_DEFAULT_SCANNOTACTIVECHANNELINTERVAL == referenceBean.getScanNotActiveChannelInterval() && springBootConsumerConfig.getScanNotActiveChannelInterval() > 0)){
            referenceBean.setScanNotActiveChannelInterval(springBootConsumerConfig().getScanNotActiveChannelInterval());
        }
        if (!referenceBean.getEnableResultCache()){
            referenceBean.setEnableResultCache(springBootConsumerConfig.getEnableResultCache());
        }
        if (referenceBean.getResultCacheExpire() <= 0
                || (RpcConstants.RPC_SCAN_RESULT_CACHE_EXPIRE == referenceBean.getResultCacheExpire() && springBootConsumerConfig.getResultCacheExpire() > 0)){
            referenceBean.setResultCacheExpire(springBootConsumerConfig.getResultCacheExpire());
        }

        if (!referenceBean.getEnableDirectServer()){
            referenceBean.setEnableDirectServer(springBootConsumerConfig.getEnableDirectServer());
        }

        if (StringUtils.isEmpty(referenceBean.getDirectServerUrl())
                || (RpcConstants.RPC_COMMON_DEFAULT_DIRECT_SERVER.equals(referenceBean.getDirectServerUrl()) && !StringUtils.isEmpty(springBootConsumerConfig.getDirectServerUrl()))){
            referenceBean.setDirectServerUrl(springBootConsumerConfig.getDirectServerUrl());

        }

        if (!referenceBean.getEnableDelayConnection()){
            referenceBean.setEnableDelayConnection(springBootConsumerConfig.getEnableDelayConnection());
        }

        if (referenceBean.getCorePoolSize() <= 0
                || (RpcConstants.DEFAULT_CORE_POOL_SIZE == referenceBean.getCorePoolSize() && springBootConsumerConfig.getCorePoolSize() > 0)){
            referenceBean.setCorePoolSize(springBootConsumerConfig.getCorePoolSize());
        }

        if (referenceBean.getMaximumPoolSize() <= 0
                || (RpcConstants.DEFAULT_MAXI_NUM_POOL_SIZE == referenceBean.getMaximumPoolSize() && springBootConsumerConfig.getMaximumPoolSize() > 0)){
            referenceBean.setMaximumPoolSize(springBootConsumerConfig.getMaximumPoolSize());
        }

        if (StringUtils.isEmpty(referenceBean.getFlowType())
                || (RpcConstants.FLOW_POST_PROCESSOR_PRINT.equals(referenceBean.getFlowType()) && !StringUtils.isEmpty(springBootConsumerConfig.getFlowType()))){
            referenceBean.setFlowType(springBootConsumerConfig.getFlowType());
        }

        if (!referenceBean.getEnableBuffer()){
            referenceBean.setEnableBuffer(springBootConsumerConfig.getEnableBuffer());
        }

        if (referenceBean.getBufferSize() <= 0
                || (RpcConstants.DEFAULT_BUFFER_SIZE == referenceBean.getBufferSize() && springBootConsumerConfig.getBufferSize() > 0)){
            referenceBean.setBufferSize(springBootConsumerConfig.getBufferSize());
        }

        if (StringUtils.isEmpty(referenceBean.getReflectType())
                || (RpcConstants.DEFAULT_REFLECT_TYPE.equals(referenceBean.getReflectType()) && !StringUtils.isEmpty(springBootConsumerConfig.getReflectType()))){
            referenceBean.setReflectType(springBootConsumerConfig.getReflectType());
        }

        if (StringUtils.isEmpty(referenceBean.getFallbackClassName())
                || (RpcConstants.DEFAULT_FALLBACK_CLASS_NAME.equals(referenceBean.getFallbackClassName()) && !StringUtils.isEmpty(springBootConsumerConfig.getFallbackClassName()))){
            referenceBean.setFallbackClassName(springBootConsumerConfig.getFallbackClassName());
        }

        if (!referenceBean.getEnableRateLimiter()){
            referenceBean.setEnableRateLimiter(springBootConsumerConfig.getEnableRateLimiter());
        }

        if (StringUtils.isEmpty(referenceBean.getRateLimiterType())
                || (RpcConstants.DEFAULT_RATELIMITER_INVOKER.equals(referenceBean.getRateLimiterType()) && !StringUtils.isEmpty(springBootConsumerConfig.getRateLimiterType()))){
            referenceBean.setRateLimiterType(springBootConsumerConfig.getRateLimiterType());
        }

        if (referenceBean.getPermits() <= 0
                || (RpcConstants.DEFAULT_RATELIMITER_PERMITS == referenceBean.getPermits() && springBootConsumerConfig.getPermits() > 0)){
            referenceBean.setPermits(springBootConsumerConfig.getPermits());
        }

        if (referenceBean.getMilliSeconds() <= 0
                || (RpcConstants.DEFAULT_RATELIMITER_MILLI_SECONDS == referenceBean.getMilliSeconds() && springBootConsumerConfig.getMilliSeconds() > 0)){
            referenceBean.setMilliSeconds(springBootConsumerConfig.getMilliSeconds());
        }

        if (StringUtils.isEmpty(referenceBean.getRateLimiterFailStrategy())
                || (RpcConstants.RATE_LIMILTER_FAIL_STRATEGY_DIRECT.equals(referenceBean.getRateLimiterFailStrategy()) && !StringUtils.isEmpty(springBootConsumerConfig.getRateLimiterFailStrategy()))){
            referenceBean.setRateLimiterFailStrategy(springBootConsumerConfig.getRateLimiterFailStrategy());
        }

        if (!referenceBean.getEnableFusing()){
            referenceBean.setEnableFusing(springBootConsumerConfig.getEnableFusing());
        }

        if (StringUtils.isEmpty(referenceBean.getFusingType())
                || (RpcConstants.DEFAULT_FUSING_INVOKER.equals(referenceBean.getFusingType()) && !StringUtils.isEmpty(springBootConsumerConfig.getFusingType()))){
            referenceBean.setFusingType(springBootConsumerConfig.getFusingType());
        }

        if (referenceBean.getTotalFailure() <= 0
                || (RpcConstants.DEFAULT_FUSING_TOTAL_FAILURE == referenceBean.getTotalFailure() && springBootConsumerConfig.getTotalFailure() > 0 )){
            referenceBean.setTotalFailure(springBootConsumerConfig.getTotalFailure());
        }

        if (referenceBean.getFusingMilliSeconds() <= 0
                || (RpcConstants.DEFAULT_FUSING_MILLI_SECONDS == referenceBean.getFusingMilliSeconds() && springBootConsumerConfig.getFusingMilliSeconds() > 0)){
            referenceBean.setFusingMilliSeconds(springBootConsumerConfig.getFusingMilliSeconds());
        }

        if (StringUtils.isEmpty(referenceBean.getExceptionPostProcessorType())
                || (RpcConstants.EXCEPTION_POST_PROCESSOR_PRINT.equals(referenceBean.getExceptionPostProcessorType()) && !StringUtils.isEmpty(springBootConsumerConfig.getExceptionPostProcessorType()))){
            referenceBean.setExceptionPostProcessorType(springBootConsumerConfig.getExceptionPostProcessorType());
        }
        return referenceBean;
    }

}
