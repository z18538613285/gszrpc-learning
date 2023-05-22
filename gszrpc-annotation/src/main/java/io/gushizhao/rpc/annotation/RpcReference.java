package io.gushizhao.rpc.annotation;

import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author gushizhao
 * @version 1.0.0
 * @description gszrpc 服务消费者
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Autowired
public @interface RpcReference {

    /**
     * 版本号
     * @return
     */
    String version() default "1.0.0";

    /**
     * 注册类型，目前的类型包含：zookeeper、nacos、etcd、consul
     * @return
     */
    String registryType() default "zookeeper";

    /**
     * 注册地址
     * @return
     */
    String registryAddress() default "127.0.0.1:2181";

    /**
     * 负载均衡类型，默认基于 ZK 的一致性Hash
     * @return
     */
    String loadBalanceType() default "zkconsistenthash";

    /**
     * 序列化类型，目前的类型包括：protostuff、kryo、json、jdk、hessian2、fastJson
     * @return
     */
    String serializationType() default "protostuff";

    /**
     * 超时时间，默认 5s
     * @return
     */
    long timeout() default 5000;

    /**
     * 是否异步执行
     * @return
     */
    boolean async() default false;

    /**
     * 是否单向调用
     * @return
     */
    boolean onwway() default false;

    /**
     * 代理类型，目前的类型包括：jdk代理、javassist代理、cglib代理
     * @return
     */
    String proxy() default "jdk";

    /**
     * 服务分组，默认为空
     * @return
     */
    String group() default "";

    int scanNotActiveChannelInterval() default 60000;
    int heartbeatInterval() default 30000;
    int retryInterval() default 1000;
    int retryTimes() default 3;
}
