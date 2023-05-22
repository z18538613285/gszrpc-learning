package io.gushizhao.rpc.loadbalancer.least.connections;

import io.gushizhao.rpc.loadbalancer.api.ServiceLoadBalancer;
import io.gushizhao.rpc.loadbalancer.base.BaseEnhancedServiceLoadBalancer;
import io.gushizhao.rpc.loadbalancer.context.ConnectionsContext;
import io.gushizhao.rpc.protocol.meta.ServiceMeta;
import io.gushizhao.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/10 10:01
 */
@SPIClass
public class LeastConnectionsServiceLoadBalancer implements ServiceLoadBalancer<ServiceMeta> {

    private final static Logger logger = LoggerFactory.getLogger(LeastConnectionsServiceLoadBalancer.class);

    @Override
    public ServiceMeta select(List<ServiceMeta> servers, int hashCode, String sourceIp) {
        logger.info("基于最少连接数的负载均衡策略...");
        if (servers == null || servers.isEmpty()) {
            return null;
        }
        ServiceMeta serviceMeta = this.getNullServiceMeta(servers);
        if (serviceMeta == null) {
            serviceMeta = this.getServiceMeta(servers);
        }
        return serviceMeta;
    }

    private ServiceMeta getServiceMeta(List<ServiceMeta> servers) {
        ServiceMeta serviceMeta = servers.get(0);
        Integer serviceMetaCount = ConnectionsContext.getValue(serviceMeta);
        for (int i = 0; i < servers.size(); i++) {
            ServiceMeta meta = servers.get(i);
            Integer metaCount = ConnectionsContext.getValue(meta);
            if (serviceMetaCount > metaCount) {
                serviceMetaCount = metaCount;
                serviceMeta = meta;
            }
        }
        return serviceMeta;
    }

    // 获取服务元数据列表中连接数为空的元数据，说明没有连接
    private ServiceMeta getNullServiceMeta(List<ServiceMeta> servers) {
        for (int i = 0; i < servers.size(); i++) {
            ServiceMeta meta = servers.get(i);
            if (ConnectionsContext.getValue(meta) == null) {
                return meta;
            }
        }
        return null;
    }

}
