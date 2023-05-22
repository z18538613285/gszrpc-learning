package io.gushizhao.rpc.loadbalancer.base;

import io.gushizhao.rpc.loadbalancer.api.ServiceLoadBalancer;
import io.gushizhao.rpc.protocol.meta.ServiceMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/12 18:30
 */
public abstract class BaseEnhancedServiceLoadBalancer implements ServiceLoadBalancer<ServiceMeta> {

    /**
     * 根据权重重新生成服务元数据列表，权重越高的元数据，会在最终的列表中出现的次数越高
     * @param servers
     * @return
     */
    protected List<ServiceMeta> getWeightServiceMetaList(List<ServiceMeta> servers) {
        if (servers == null || servers.isEmpty()) {
            return null;
        }
        List<ServiceMeta> serviceMetaList = new ArrayList<>();
        servers.stream().forEach((server) -> {
            IntStream.range(0, server.getWeight()).forEach((i) -> {
                serviceMetaList.add(server);
            });
        });
        return serviceMetaList;
    }
}
