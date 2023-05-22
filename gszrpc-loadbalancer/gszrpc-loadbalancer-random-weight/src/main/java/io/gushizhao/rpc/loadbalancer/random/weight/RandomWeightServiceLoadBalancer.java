package io.gushizhao.rpc.loadbalancer.random.weight;

import io.gushizhao.rpc.loadbalancer.api.ServiceLoadBalancer;
import io.gushizhao.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/10 10:01
 */
@SPIClass
public class RandomWeightServiceLoadBalancer<T> implements ServiceLoadBalancer<T> {

    private final static Logger logger = LoggerFactory.getLogger(RandomWeightServiceLoadBalancer.class);

    @Override
    public T select(List<T> servers, int hashCode, String sourceIp) {
        logger.info("基于加权随机算法的负载均衡策略...");
        if (servers == null || servers.isEmpty()) {
            return null;
        }
        /**
         * 最核心的逻辑就是获取到传入的服务提供者实例列表的前（hasCode % servers.size（））个服务提供者
         * 从中随机选择一个，缩小了随机的范围
         */
        hashCode = Math.abs(hashCode);
        int count = hashCode % servers.size();
        if (count <= 1) {
            count = servers.size();
        }
        Random random = new Random();
        int index = random.nextInt(count);
        return servers.get(index);
    }
}
