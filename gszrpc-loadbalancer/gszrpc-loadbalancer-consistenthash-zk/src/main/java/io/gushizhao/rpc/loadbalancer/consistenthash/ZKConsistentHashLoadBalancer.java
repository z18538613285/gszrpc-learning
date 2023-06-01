package io.gushizhao.rpc.loadbalancer.consistenthash;

import io.gushizhao.rpc.loadbalancer.api.ServiceLoadBalancer;
import io.gushizhao.rpc.protocol.meta.ServiceMeta;
import io.gushizhao.rpc.spi.annotation.SPIClass;
import org.apache.curator.x.discovery.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/10 10:01
 */
@SPIClass
public class ZKConsistentHashLoadBalancer<T> implements ServiceLoadBalancer<T> {

    private final static Logger logger = LoggerFactory.getLogger(ZKConsistentHashLoadBalancer.class);

    private final static int VIRTUAL_NODE_SIZE = 10;
    private final static String VIRTUAL_NODE_SPLIT = "#";

    @Override
    public T select(List<T> servers, int hashCode, String sourceIp) {
        logger.info("基于Zookeeper的一致性Hash算法的负载均衡策略...");
        TreeMap<Integer,T> ring = makeConsistentHashRing(servers);
        return allocateNode(ring, hashCode);
    }

    private TreeMap<Integer, T> makeConsistentHashRing(List<T> servers) {
        TreeMap<Integer, T> ring = new TreeMap<>();
        for (T instance : servers) {
            for (int i = 0; i < VIRTUAL_NODE_SIZE; i++) {
                ring.put((buildServiceInstanceKey(instance) + VIRTUAL_NODE_SPLIT + i).hashCode(), instance);
            }
        }
        return ring;
    }

    private String buildServiceInstanceKey(T instance) {
        return Objects.toString(instance);
    }

    private T allocateNode(TreeMap<Integer, T> ring, int hashCode) {
        Map.Entry<Integer, T> entry = ring.ceilingEntry(hashCode);
        if (entry == null) {
            entry = ring.firstEntry();
        }
        if (entry == null) {
            throw new RuntimeException("not discover useful service, please register service in registry center.");
        }
        return entry.getValue();
    }
}
