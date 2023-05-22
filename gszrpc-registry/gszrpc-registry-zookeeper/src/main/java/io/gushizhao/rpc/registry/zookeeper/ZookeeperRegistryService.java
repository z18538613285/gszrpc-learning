package io.gushizhao.rpc.registry.zookeeper;

import io.gushizhao.rpc.common.helper.RpcServiceHelper;
import io.gushizhao.rpc.constants.RpcConstants;
import io.gushizhao.rpc.loadbalancer.api.ServiceLoadBalancer;
import io.gushizhao.rpc.loadbalancer.helper.ServiceLoadBalanceHelper;
import io.gushizhao.rpc.loadbalancer.random.RandomServiceLoadBalancer;
import io.gushizhao.rpc.protocol.meta.ServiceMeta;
import io.gushizhao.rpc.registry.api.RegistryService;
import io.gushizhao.rpc.registry.api.config.RegistryConfig;
import io.gushizhao.rpc.spi.annotation.SPIClass;
import io.gushizhao.rpc.spi.loader.ExtensionLoader;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.springframework.util.backoff.ExponentialBackOff;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static org.apache.curator.x.discovery.ServiceDiscoveryBuilder.*;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/8 15:55
 */
@SPIClass
public class ZookeeperRegistryService implements RegistryService {
    // 初始化 CuratorFramework 客户端，进行连接重试的间隔时间。
    public static final int BASE_SLEEP_TIME_MS = 1000;
    // 初始化 CuratorFramework 客户端，进行连接重试的最大重试次数
    public static final int MAX_RETRIES = 3;

    // 服务注册到 zookeeper 的根路径
    public static final String ZK_BASE_PATH = "/gushizhao_rpc";
    // 服务注册与发现的 ServiceDiscovery 类实例
    private ServiceDiscovery<ServiceMeta> serviceDiscovery;

    private ServiceLoadBalancer<ServiceInstance<ServiceMeta>> serviceLoadBalancer;

    private ServiceLoadBalancer<ServiceMeta> serviceEnhancedLoadBalancer;

    /**
     * 主要是构建 CuratorFramework 客户端， 并初始化 serviceDiscovery
     * @param registryConfig
     * @throws Exception
     */
    @Override
    public void init(RegistryConfig registryConfig) throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(
                registryConfig.getRegistryAddr(),
                new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRIES));
        client.start();
        JsonInstanceSerializer<ServiceMeta> serializer = new JsonInstanceSerializer<>(ServiceMeta.class);
        this.serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceMeta.class)
                .client(client)
                .serializer(serializer)
                .basePath(ZK_BASE_PATH)
                .build();
        this.serviceDiscovery.start();
        if (registryConfig.getRegistryLoadBalanceType().toLowerCase().contains(RpcConstants.SERVICE_ENHANCED_LOAD_BALANCER_PREFIX)) {
            this.serviceEnhancedLoadBalancer = ExtensionLoader.getExtension(ServiceLoadBalancer.class, registryConfig.getRegistryLoadBalanceType());
        } else {
            this.serviceLoadBalancer = ExtensionLoader.getExtension(ServiceLoadBalancer.class, registryConfig.getRegistryLoadBalanceType());
        }
    }

    /**
     * 主要是使用 serviceDiscovery 将 serviceMeta 元数据注册到 Zookeeper 中
     * @param serviceMeta 服务元数据
     * @throws Exception
     */
    @Override
    public void register(ServiceMeta serviceMeta) throws Exception {
        ServiceInstance<ServiceMeta> serviceInstance = ServiceInstance
                .<ServiceMeta>builder()
                .name(RpcServiceHelper.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion(), serviceMeta.getServiceGroup()))
                .address(serviceMeta.getServiceAddr())
                .port(serviceMeta.getServicePort())
                .payload(serviceMeta)
                .build();
        serviceDiscovery.registerService(serviceInstance);
    }

    /**
     * 主要是移除Zookeeper 中注册的对应的元数据
     * @param serviceMeta 服务元数据
     * @throws Exception
     */
    @Override
    public void unRegister(ServiceMeta serviceMeta) throws Exception {
        ServiceInstance<ServiceMeta> serviceInstance = ServiceInstance
                .<ServiceMeta>builder()
                .name(serviceMeta.getServiceName())
                .address(serviceMeta.getServiceAddr())
                .port(serviceMeta.getServicePort())
                .payload(serviceMeta)
                .build();
        serviceDiscovery.unregisterService(serviceInstance);
    }

    /**
     * 获取 serviceMeta 元数据信息
     * @param serviceName 服务名称
     * @param invokeHashCode HashCode 值
     * @return
     * @throws Exception
     */
    @Override
    public ServiceMeta discovery(String serviceName, int invokeHashCode, String sourceIp) throws Exception {
        Collection<ServiceInstance<ServiceMeta>> serviceInstances = serviceDiscovery.queryForInstances(serviceName);
        if (serviceLoadBalancer != null) {
            return getServiceMetaInstance(invokeHashCode, sourceIp, (List<ServiceInstance<ServiceMeta>>) serviceInstances);
        }

        return this.serviceEnhancedLoadBalancer.select(ServiceLoadBalanceHelper.getServiceMetaList((List<ServiceInstance<ServiceMeta>>) serviceInstances), invokeHashCode, sourceIp);
    }

    private ServiceMeta getServiceMetaInstance(int invokeHashCode, String sourceIp, List<ServiceInstance<ServiceMeta>> serviceInstances) {
        ServiceInstance<ServiceMeta> instance = this.serviceLoadBalancer.select(serviceInstances, invokeHashCode, sourceIp);
        if (instance != null) {
            return instance.getPayload();
        }
        /**
         * 这里并，诶呦使用 invokeHashCode ，会在后续扩展负载均衡策略时使用
         */
        return null;
    }


    /**
     * 主要是调用 serviceDiscovery 对象的 close 方法，关闭与 Zookeeper 的连接
     * @throws IOException
     */
    @Override
    public void destroy() throws IOException {
        serviceDiscovery.close();
    }
}
