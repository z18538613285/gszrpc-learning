package io.gushizhao.rpc.enhanced.loadbalancer.sourceip.hash;

import io.gushizhao.rpc.loadbalancer.base.BaseEnhancedServiceLoadBalancer;
import io.gushizhao.rpc.protocol.meta.ServiceMeta;
import io.gushizhao.rpc.spi.annotation.SPIClass;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/10 10:01
 */
@SPIClass
public class SourceIpHashWeightServiceEnhancerLoadBalancer extends BaseEnhancedServiceLoadBalancer {

    private final static Logger logger = LoggerFactory.getLogger(SourceIpHashWeightServiceEnhancerLoadBalancer.class);

    @Override
    public ServiceMeta select(List<ServiceMeta> servers, int hashCode, String sourceIp) {
        logger.info("基于增强型加权源IP地址Hash的负载均衡策略...");
        servers = this.getWeightServiceMetaList(servers);
        if (servers == null || servers.isEmpty()) {
            return null;
        }
        if (StringUtils.isEmpty(sourceIp)) {
            return servers.get(0);
        }
        int resultHashCode = Math.abs(sourceIp.hashCode() + hashCode);
        return servers.get(resultHashCode % servers.size());
    }
}
