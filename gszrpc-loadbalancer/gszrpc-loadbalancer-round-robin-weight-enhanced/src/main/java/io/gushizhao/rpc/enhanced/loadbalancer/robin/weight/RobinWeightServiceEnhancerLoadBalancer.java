package io.gushizhao.rpc.enhanced.loadbalancer.robin.weight;

import io.gushizhao.rpc.loadbalancer.base.BaseEnhancedServiceLoadBalancer;
import io.gushizhao.rpc.protocol.meta.ServiceMeta;
import io.gushizhao.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/10 10:01
 */
@SPIClass
public class RobinWeightServiceEnhancerLoadBalancer extends BaseEnhancedServiceLoadBalancer {

    private final static Logger logger = LoggerFactory.getLogger(RobinWeightServiceEnhancerLoadBalancer.class);

    private volatile AtomicInteger atomicInteger = new AtomicInteger(0);


    @Override
    public ServiceMeta select(List<ServiceMeta> servers, int hashCode, String sourceIp) {
        logger.info("基于增强型加权轮询算法的负载均衡策略...");
        servers = this.getWeightServiceMetaList(servers);
        if (servers == null || servers.isEmpty()) {
            return null;
        }
        int index = atomicInteger.incrementAndGet();
        if (index >= Integer.MAX_VALUE - 10000) {
            atomicInteger.set(0);
        }
        return servers.get(index % servers.size());
    }
}
