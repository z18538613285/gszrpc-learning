package io.gushizhao.rpc.proxy.api.config;

import io.gushizhao.rpc.proxy.api.consumer.Consumer;
import io.gushizhao.rpc.registry.api.RegistryService;

import java.io.Serializable;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/8 14:41
 */
public class ProxyConfig<T> implements Serializable {

    private static final long serialVersionUID = 6684321684652L;

    /**
     * 接口的 Class 对象
     */
    private Class<T> clazz;


    private String serviceVersion;
    private String serviceGroup;
    // 超时时间
    private long timeout;
    private RegistryService registryService;
    // 服务消费者
    private Consumer consumer;
    // 序列化类型
    private String serializationType;
    private boolean async;
    private boolean oneway;

    public ProxyConfig(Class<T> clazz, String serviceVersion, String serviceGroup, long timeout, RegistryService registryService, Consumer consumer, String serializationType, boolean async, boolean oneway) {
        this.clazz = clazz;
        this.serviceVersion = serviceVersion;
        this.serviceGroup = serviceGroup;
        this.timeout = timeout;
        this.consumer = consumer;
        this.serializationType = serializationType;
        this.async = async;
        this.oneway = oneway;
        this.registryService = registryService;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public void setClazz(Class<T> clazz) {
        this.clazz = clazz;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public String getServiceGroup() {
        return serviceGroup;
    }

    public void setServiceGroup(String serviceGroup) {
        this.serviceGroup = serviceGroup;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    public String getSerializationType() {
        return serializationType;
    }

    public void setSerializationType(String serializationType) {
        this.serializationType = serializationType;
    }

    public boolean getAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public boolean getOneway() {
        return oneway;
    }

    public void setOneway(boolean oneway) {
        this.oneway = oneway;
    }
}
