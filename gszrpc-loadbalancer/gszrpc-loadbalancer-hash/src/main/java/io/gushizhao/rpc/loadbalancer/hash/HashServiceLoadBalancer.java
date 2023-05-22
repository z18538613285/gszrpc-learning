package io.gushizhao.rpc.loadbalancer.hash;

import io.gushizhao.rpc.loadbalancer.api.ServiceLoadBalancer;
import io.gushizhao.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/10 10:01
 */
@SPIClass
public class HashServiceLoadBalancer<T> implements ServiceLoadBalancer<T> {

    private final static Logger logger = LoggerFactory.getLogger(HashServiceLoadBalancer.class);


    @Override
    public T select(List<T> servers, int hashCode, String sourceIp) {
        logger.info("基于Hash算法的负载均衡策略...");
        if (servers == null || servers.isEmpty()) {
            return null;
        }

        int index = Math.abs(hashCode) % servers.size();
        return servers.get(index);
    }
}
