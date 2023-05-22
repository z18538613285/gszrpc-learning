package io.gushizhao.rpc.loadbalancer.robin;

import io.gushizhao.rpc.loadbalancer.api.ServiceLoadBalancer;
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
public class RobinServiceLoadBalancer<T> implements ServiceLoadBalancer<T> {

    private final static Logger logger = LoggerFactory.getLogger(RobinServiceLoadBalancer.class);

    private volatile AtomicInteger atomicInteger = new AtomicInteger(0);

    @Override
    public T select(List<T> servers, int hashCode, String sourceIp) {
        logger.info("基于轮询算法的负载均衡策略...");
        if (servers == null || servers.isEmpty()) {
            return null;
        }
        int count = servers.size();
        int index = atomicInteger.incrementAndGet();
        // 为了避免在高并发下由于竞态条件导致 AtomicInteger 自增后超过 Integer 的最大值 从而发生范围越界的问题。
        if (index >= (Integer.MAX_VALUE - 10000)) {
            atomicInteger.set(0);
        }
        return servers.get(index % count);
    }
}
