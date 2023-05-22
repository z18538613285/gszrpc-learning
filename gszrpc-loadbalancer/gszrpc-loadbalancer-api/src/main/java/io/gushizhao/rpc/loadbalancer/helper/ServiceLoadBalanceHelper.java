package io.gushizhao.rpc.loadbalancer.helper;

import io.gushizhao.rpc.protocol.meta.ServiceMeta;
import org.apache.curator.x.discovery.ServiceInstance;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Author huzhichao
 * @Description 主要的逻辑就是将 List<ServiceInstance<ServiceMeta>> 列表转换成 List<ServiceMeta> 列表
 *              在增强型负载均衡的实现中，接口返回的对象会被直接定义为 ServiceMeta
 * @Date 2023/5/12 18:40
 */
public class ServiceLoadBalanceHelper {

    private static volatile List<ServiceMeta> cacheServiceMeta = new CopyOnWriteArrayList<>();

    public static List<ServiceMeta> getServiceMetaList(List<ServiceInstance<ServiceMeta>> serviceInstances) {
        if (serviceInstances == null || serviceInstances.isEmpty()) {
            return cacheServiceMeta;
        }
        // 先清空cacheServiceMeta 中的数据
        cacheServiceMeta.clear();
        serviceInstances.stream().forEach((serviceMetaServiceInstance -> {
            cacheServiceMeta.add(serviceMetaServiceInstance.getPayload());
        }));
        return cacheServiceMeta;
    }
}
