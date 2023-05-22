package io.gushizhao.rpc.protocol.meta;

import java.io.Serializable;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/8 15:30
 */
public class ServiceMeta implements Serializable {
    private static final long serialVersionUID = 3578438435138545L;

    private String serviceName;
    private String serviceVersion;
    private String serviceAddr;
    private int servicePort;
    private String serviceGroup;
    private int weight;

    /**
     * 被转换json的类中，使用了带参数的构造方法，而忘记初始化原始构造方法，即，这个类没有了无参的初始化构造方法；
     *
     * 这种情况下，无法对该类进行实例化，没有办法进行toJson操作。
     */
    public ServiceMeta() {
    }

    public ServiceMeta(String serviceName, String serviceVersion, String serviceAddr, int servicePort, String serviceGroup, int weight) {
        this.serviceName = serviceName;
        this.serviceVersion = serviceVersion;
        this.serviceAddr = serviceAddr;
        this.servicePort = servicePort;
        this.serviceGroup = serviceGroup;
        this.weight = weight;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public String getServiceAddr() {
        return serviceAddr;
    }

    public void setServiceAddr(String serviceAddr) {
        this.serviceAddr = serviceAddr;
    }

    public int getServicePort() {
        return servicePort;
    }

    public void setServicePort(int servicePort) {
        this.servicePort = servicePort;
    }

    public String getServiceGroup() {
        return serviceGroup;
    }

    public void setServiceGroup(String serviceGroup) {
        this.serviceGroup = serviceGroup;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
