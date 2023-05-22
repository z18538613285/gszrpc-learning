package io.gushizhao.rpc.consumer.spring;

import io.gushizhao.rpc.consumer.RpcClient;
import io.gushizhao.rpc.proxy.api.consumer.Consumer;
import io.gushizhao.rpc.registry.api.RegistryService;
import org.springframework.beans.factory.FactoryBean;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/19 16:16
 */
public class RpcReferenceBean implements FactoryBean<Object> {

    /**
     * 接口的 Class 对象
     */
    private Class<?> interfaceClass;

    private String version;

    private String registryType;

    private String loadBalanceType;

    private String serializationType;

    private String registryAddress;

    private String group;

    private long timeout;

    private boolean async;

    private boolean oneway;

    private String proxy;

    private Object object;

    private int scanNotActiveChannelInterval;

    private int heartbeatInterval;

    private int retryInterval = 1000;

    private int retryTimes = 3;

    @Override
    public Object getObject() throws Exception {
        return object;
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }

    @SuppressWarnings("unchecked")
    public void init() throws Exception {
       RpcClient rpcClient = new RpcClient(registryAddress, registryType, loadBalanceType, proxy, version, group, timeout, serializationType, async, oneway, heartbeatInterval, scanNotActiveChannelInterval, retryInterval, retryTimes);
       this.object = rpcClient.create(interfaceClass);
    }

    public void setInterfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setRegistryType(String registryType) {
        this.registryType = registryType;
    }

    public void setLoadBalanceType(String loadBalanceType) {
        this.loadBalanceType = loadBalanceType;
    }

    public void setSerializationType(String serializationType) {
        this.serializationType = serializationType;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public void setOneway(boolean oneway) {
        this.oneway = oneway;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public void setScanNotActiveChannelInterval(int scanNotActiveChannelInterval) {
        this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
    }

    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }
}
