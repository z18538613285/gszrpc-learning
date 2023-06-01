package io.gushizhao.rpc.provider.starter;

import io.gushizhao.rpc.provider.config.SpringBootProviderConfig;
import io.gushizhao.rpc.provider.spring.RpcSpringServer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/22 14:21
 */
@Configuration
public class SpringBootProviderAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "gszrpc.gushizhao.provider")
    public SpringBootProviderConfig springBootProviderConfig() {
        return new SpringBootProviderConfig();
    }

    @Bean
    public RpcSpringServer rpcSpringServer(final SpringBootProviderConfig springBootProviderConfig) {
        return new RpcSpringServer(springBootProviderConfig.getServerAddress(),
                springBootProviderConfig.getRegistryAddress(),
                springBootProviderConfig.getRegistryType(),
                springBootProviderConfig.getRegistryLoadBalanceType(),
                springBootProviderConfig.getReflectType(),
                springBootProviderConfig.getHeartbeatInterval(),
                springBootProviderConfig.getScanNotActiveChannelInterval(),
                springBootProviderConfig.isEnableResultCache(),
                springBootProviderConfig().getResultCacheExpire(),
                springBootProviderConfig.getCorePoolSize(),
                springBootProviderConfig.getMaximumPoolSize(),
                springBootProviderConfig.getFlowType(),
                springBootProviderConfig.getMaxConnections(),
                springBootProviderConfig.getDisuseStrategyType(),
                springBootProviderConfig.getEnableBuffer(),
                springBootProviderConfig.getBufferSize(),
                springBootProviderConfig.getEnableRateLimiter(),
                springBootProviderConfig.getRateLimiterType(),
                springBootProviderConfig.getPermits(),
                springBootProviderConfig.getMilliSeconds(),
                springBootProviderConfig.getRateLimiterFailStrategy(),
                springBootProviderConfig.getEnableFusing(),
                springBootProviderConfig.getFusingType(),
                springBootProviderConfig.getTotalFailure(),
                springBootProviderConfig.getFusingMilliSeconds(),
                springBootProviderConfig.getExceptionPostProcessorType());
    }
}
