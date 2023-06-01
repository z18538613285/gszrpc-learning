package io.gushizhao.rpc.annotation;

import io.gushizhao.rpc.constants.RpcConstants;
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

    boolean enableResultCache() default false;
    int resultCacheExpire() default RpcConstants.RPC_SCAN_RESULT_CACHE_EXPIRE;

    // 是否开启直连服务
    boolean enableDirectServer() default false;
    // 直连服务的地址
    String directServerUrl() default RpcConstants.RPC_COMMON_DEFAULT_DIRECT_SERVER;

    // 是否开启延迟连接
    boolean enableDelayConnection() default false;

    int corePoolSize() default RpcConstants.DEFAULT_CORE_POOL_SIZE; ;

    int maximumPoolSize() default RpcConstants.DEFAULT_MAXI_NUM_POOL_SIZE;

    String flowType() default RpcConstants.FLOW_POST_PROCESSOR_PRINT;

    boolean enableBuffer() default false;

    int bufferSize() default RpcConstants.DEFAULT_BUFFER_SIZE;

    Class<?> fallbackClass() default void.class;

    String fallbackClassName() default RpcConstants.DEFAULT_FALLBACK_CLASS_NAME;

    String reflectType() default RpcConstants.DEFAULT_REFLECT_TYPE;

    boolean enableRateLimiter() default false;

    String rateLimiterType() default RpcConstants.DEFAULT_RATELIMITER_INVOKER;

    int permits() default RpcConstants.DEFAULT_RATELIMITER_PERMITS;

    int milliSeconds() default RpcConstants.DEFAULT_RATELIMITER_MILLI_SECONDS;

    String rateLimiterFailStrategy() default RpcConstants.RATE_LIMILTER_FAIL_STRATEGY_DIRECT;

    boolean enableFusing() default false;

    String fusingType() default RpcConstants.DEFAULT_FUSING_INVOKER;

    double totalFailure() default RpcConstants.DEFAULT_FUSING_TOTAL_FAILURE;

    int fusingMilliSeconds() default RpcConstants.DEFAULT_FUSING_MILLI_SECONDS;

    String exceptionPostProcessorType() default RpcConstants.EXCEPTION_POST_PROCESSOR_PRINT;
}
