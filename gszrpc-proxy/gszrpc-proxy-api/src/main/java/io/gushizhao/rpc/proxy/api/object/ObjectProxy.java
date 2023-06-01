package io.gushizhao.rpc.proxy.api.object;

import io.gushizhao.rpc.cache.result.CacheResultKey;
import io.gushizhao.rpc.cache.result.CacheResultManager;
import io.gushizhao.rpc.common.exception.RpcException;
import io.gushizhao.rpc.constants.RpcConstants;
import io.gushizhao.rpc.exception.processor.ExceptionPostProcessor;
import io.gushizhao.rpc.fusing.api.FusingInvoker;
import io.gushizhao.rpc.protocol.RpcProtocol;
import io.gushizhao.rpc.protocol.enumeration.RpcStatus;
import io.gushizhao.rpc.protocol.enumeration.RpcType;
import io.gushizhao.rpc.protocol.header.RpcHeader;
import io.gushizhao.rpc.protocol.header.RpcHeaderFactory;
import io.gushizhao.rpc.protocol.request.RpcRequest;
import io.gushizhao.rpc.protocol.response.RpcResponse;
import io.gushizhao.rpc.proxy.api.async.IAsyncObjectProxy;
import io.gushizhao.rpc.proxy.api.consumer.Consumer;
import io.gushizhao.rpc.proxy.api.future.RPCFuture;
import io.gushizhao.rpc.ratelimiter.api.RateLimiterInvoker;
import io.gushizhao.rpc.reflect.api.ReflectInvoker;
import io.gushizhao.rpc.registry.api.RegistryService;
import io.gushizhao.rpc.spi.loader.ExtensionLoader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/4 17:14
 */
public class ObjectProxy<T> implements IAsyncObjectProxy, InvocationHandler {

    private final Logger logger = LoggerFactory.getLogger(ObjectProxy.class);

    private RegistryService registryService;

    /**
     * 接口的 Class 对象
     */
    private Class<T> clazz;


    private String serviceVersion;
    private String serviceGroup;
    // 超时时间
    private long timeout = 15000;
    // 服务消费者
    private Consumer consumer;
    // 序列化类型
    private String serializationType;
    private boolean async;
    private boolean oneway;

    private boolean enableResultCache;

    private CacheResultManager<Object> cacheResultManager;


    private ReflectInvoker reflectInvoker;

    private Class<?> fallbackClass;

    /**
     * 限流规则SPI接口
     */
    private RateLimiterInvoker rateLimiterInvoker;

    /**
     * 是否开启限流
     */
    private boolean enableRateLimiter;

    private String rateLimiterFailStrategy;
    private boolean enableFusing;

    private FusingInvoker fusingInvoker;

    private ExceptionPostProcessor exceptionPostProcessor;

    public ObjectProxy(Class<T> clazz) {
        this.clazz = clazz;
    }

    public ObjectProxy(Class<T> clazz, String serviceVersion, String serviceGroup, long timeout, RegistryService registryService,
                       Consumer consumer, String serializationType, boolean async, boolean oneway, boolean enableResultCache,
                       int resultCacheExpire, String reflectType, String fallbackClassName, Class<?> fallbackClass,
                       boolean enableRateLimiter, String rateLimiterType, int permits, int milliSeconds, String rateLimiterFailStrategy,
                       boolean enableFusing, String fusingType, double totalFailure, int fusingMilliSeconds, String exceptionPostProcessorType) {
        this.clazz = clazz;
        this.serviceVersion = serviceVersion;
        this.serviceGroup = serviceGroup;
        this.timeout = timeout;
        this.consumer = consumer;
        this.serializationType = serializationType;
        this.async = async;
        this.oneway = oneway;
        this.registryService = registryService;
        this.enableResultCache = enableResultCache;
        if (resultCacheExpire <= 0) {
            resultCacheExpire = RpcConstants.RPC_SCAN_RESULT_CACHE_EXPIRE;
        }
        this.cacheResultManager = CacheResultManager.getInstance(resultCacheExpire, enableResultCache);
        this.reflectInvoker = ExtensionLoader.getExtension(ReflectInvoker.class, reflectType);
        this.fallbackClass = this.getFallbackClass(fallbackClassName, fallbackClass);
        this.enableRateLimiter = enableRateLimiter;
        this.initRateLimiter(rateLimiterType, permits, milliSeconds);
        if (StringUtils.isEmpty(rateLimiterFailStrategy)) {
            rateLimiterFailStrategy = RpcConstants.RATE_LIMILTER_FAIL_STRATEGY_DIRECT;
        }
        this.rateLimiterFailStrategy = rateLimiterFailStrategy;
        this.enableFusing = enableFusing;
        this.initFusing(fusingType, totalFailure, fusingMilliSeconds);
        if (StringUtils.isEmpty(exceptionPostProcessorType)) {
            exceptionPostProcessorType = RpcConstants.EXCEPTION_POST_PROCESSOR_PRINT;
        }
        this.exceptionPostProcessor = ExtensionLoader.getExtension(ExceptionPostProcessor.class, exceptionPostProcessorType);
    }

    /**
     * 初始化熔断SPI接口
     */
    private void initFusing(String fusingType, double totalFailure, int fusingMilliSeconds) {
        if (enableFusing) {
            fusingType = StringUtils.isEmpty(fusingType) ? RpcConstants.DEFAULT_FUSING_INVOKER : fusingType;
            this.fusingInvoker = ExtensionLoader.getExtension(FusingInvoker.class, fusingType);
            this.fusingInvoker.init(totalFailure, fusingMilliSeconds);
        }
    }

    /**
     * 初始化限流器
     */
    private void initRateLimiter(String rateLimiterType, int permits, int milliSeconds) {
        if (enableRateLimiter) {
            rateLimiterType = StringUtils.isEmpty(rateLimiterType) ? RpcConstants.DEFAULT_RATELIMITER_INVOKER : rateLimiterType;
            this.rateLimiterInvoker = ExtensionLoader.getExtension(RateLimiterInvoker.class, rateLimiterType);
            this.rateLimiterInvoker.init(permits, milliSeconds);
        }
    }

    /**
     * 以容错方式真正发送请求
     */
    private Object invokeSendRequestMethodWithFallback(Method method, Object[] args) throws Exception {
        try {
            return invokeSendRequestMethodWithRateLimiter(method, args);
        } catch (Throwable t) {
            exceptionPostProcessor.postExceptionProcessor(t);
            // fallbackClass 不为空，则执行容错处理
            if (this.isFallbackClassEmpty(fallbackClass)) {
                return null;
            }
            return getFallbackResult(method, args);
        }
    }


    /**
     * 以限流方式发送请求
     */
    private Object invokeSendRequestMethodWithRateLimiter(Method method, Object[] args)  throws Exception {
        Object result = null;
        if (enableRateLimiter) {
            if (rateLimiterInvoker.tryAcquire()) {
                try {
                    result = invokeSendRequestMethodWithFusing(method, args);
                } finally {
                    rateLimiterInvoker.release();
                }
            } else {
                // TODO 执行各种策略
                result = this.invokeFailRateLimiterMethod(method, args);
            }
        } else {
            result = invokeSendRequestMethodWithFusing(method, args);
        }
        return result;
    }

    private Object invokeSendRequestMethod(Method method, Object[] args) throws Exception {
        //try {
        RpcProtocol<RpcRequest> requestRpcProtocol = getSendRequest(method, args);

        RPCFuture rpcFuture = this.consumer.sendRequest(requestRpcProtocol, registryService);
        return rpcFuture == null ? null : timeout > 0 ? rpcFuture.get(timeout, TimeUnit.MILLISECONDS) : rpcFuture.get();
        //} catch (Throwable t) {
        //    // fallbackClass 不为空，则执行容错处理
        //    if (this.isFallbackClassEmpty(fallbackClass)) {
        //        return null;
        //    }
        //    return getFallbackResult(method, args);
        //}

    }


    /**
     * 封装请求协议对象
     */
    private RpcProtocol<RpcRequest> getSendRequest(Method method, Object[] args) {

        RpcProtocol<RpcRequest> requestRpcProtocol = new RpcProtocol<>();
        requestRpcProtocol.setHeader(RpcHeaderFactory.getRequestHeader(serializationType, RpcType.REQUEST.getType()));

        RpcRequest request = new RpcRequest();
        request.setClassName(method.getDeclaringClass().getName());
        request.setGroup(this.serviceGroup);
        request.setMethodName(method.getName());
        request.setParameters(args);
        request.setParameterTypes(method.getParameterTypes());
        request.setVersion(this.serviceVersion);
        request.setAsync(async);
        request.setOneway(oneway);
        requestRpcProtocol.setBody(request);

        // debug
        logger.debug(method.getDeclaringClass().getName());
        logger.debug(method.getName());

        if (method.getParameterTypes() != null && method.getParameterTypes().length > 0) {
            for (int i = 0; i < method.getParameterTypes().length; i++) {
                logger.debug(method.getParameterTypes()[i].getName());
            }
        }
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                logger.debug(args[i].toString());
            }
        }
        return requestRpcProtocol;
    }

    private Object invokeSendRequestMethodCache(Method method, Object[] args) throws Throwable {
        // 开启缓存，则处理缓存
        CacheResultKey cacheResultKey = new CacheResultKey(method.getDeclaringClass().getName(), method.getName(), method.getParameterTypes(), args, serviceVersion, serviceGroup);
        Object obj = this.cacheResultManager.get(cacheResultKey);
        if (obj == null) {
            obj = invokeSendRequestMethodWithFallback(method, args);
            if (obj != null) {
                cacheResultKey.setCacheTimeStamp(System.currentTimeMillis());
                this.cacheResultManager.put(cacheResultKey, obj);
            }
        }
        return obj;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            if ("equals".equals(name)) {
                return proxy == args[0];
            } else if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            } else if ("toString".equals(name)) {
                return proxy.getClass().getName() + "@" +
                        Integer.toHexString(System.identityHashCode(proxy)) +
                        ", with InvocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }

        if (enableResultCache) {
            return invokeSendRequestMethodCache(method, args);
        }
        return invokeSendRequestMethodWithFallback(method, args);
    }

    @Override
    public RPCFuture call(String funcName, Object... args) {
        RpcProtocol<RpcRequest> request = createRequest(this.clazz.getName(), funcName, args);

        RPCFuture rpcFuture = null;
        try {
            rpcFuture = this.consumer.sendRequest(request, registryService);
        } catch (Exception e) {
            exceptionPostProcessor.postExceptionProcessor(e);
            logger.error("async all throws exception:{}", e);
        }
        return rpcFuture;
    }


    private RpcProtocol<RpcRequest> createRequest(String className, String methodName, Object[] args) {
        RpcProtocol<RpcRequest> requestRpcProtocol = new RpcProtocol<>();
        requestRpcProtocol.setHeader(RpcHeaderFactory.getRequestHeader(serializationType, RpcType.REQUEST.getType()));

        RpcRequest request = new RpcRequest();
        request.setClassName(className);
        request.setGroup(this.serviceGroup);
        request.setMethodName(methodName);
        request.setParameters(args);
        request.setVersion(this.serviceVersion);

        Class[] paramterTypes = new Class[args.length];
        // Get the right class type
        for (int i = 0; i < args.length; i++) {
            paramterTypes[i] = getClassType(args[i]);
        }
        request.setParameterTypes(paramterTypes);
        requestRpcProtocol.setBody(request);
        // debug
        logger.debug(className);
        logger.debug(methodName);

        for (int i = 0; i < paramterTypes.length; i++) {
            logger.debug(paramterTypes[i].getName());
        }
        for (int i = 0; i < args.length; i++) {
            logger.debug(args[i].toString());
        }
        return requestRpcProtocol;
    }

    private Class<?> getClassType(Object obj) {
        Class<?> classType = obj.getClass();
        String typeName = classType.getName();
        switch (typeName) {
            case "java.lang.Integer":
                return Integer.TYPE;
            case "java.lang.Long":
                return Long.TYPE;
            case "java.lang.Float":
                return Float.TYPE;
            case "java.lang.Double":
                return Double.TYPE;
            case "java.lang.Character":
                return Character.TYPE;
            case "java.lang.Boolean":
                return Boolean.TYPE;
            case "java.lang.Short":
                return Short.TYPE;
            case "java.lang.Byte":
                return Byte.TYPE;
        }
        return classType;
    }


    /**
     * 优先使用fallbackClass，如果fallbackClass为空，则使用fallbackClassName
     */
    private Class<?> getFallbackClass(String fallbackClassName, Class<?> fallbackClass) {
        if (this.isFallbackClassEmpty(fallbackClass)) {
            try {
                if (!StringUtils.isEmpty(fallbackClassName)) {
                    fallbackClass = Class.forName(fallbackClassName);
                }
            } catch (ClassNotFoundException e) {
                exceptionPostProcessor.postExceptionProcessor(e);
                logger.error(e.getMessage());
            }
        }
        return fallbackClass;
    }

    /**
     * 容错class为空
     */
    private boolean isFallbackClassEmpty(Class<?> fallbackClass) {
        return fallbackClass == null
                || fallbackClass == RpcConstants.DEFAULT_FALLBACK_CLASS
                || RpcConstants.DEFAULT_FALLBACK_CLASS.equals(fallbackClass);
    }

    /**
     * 获取容错结果
     */
    private Object getFallbackResult(Method method, Object[] args) {
        try {
            //fallbackClass不为空，则执行容错处理
            if (this.isFallbackClassEmpty(fallbackClass)) {
                return null;
            }
            return reflectInvoker.invokeMethod(fallbackClass.newInstance(), fallbackClass, method.getName(), method.getParameterTypes(), args);
        } catch (Throwable ex) {
            exceptionPostProcessor.postExceptionProcessor(ex);
            logger.error(ex.getMessage());
        }
        return null;
    }


    /**
     * 执行限流失败时的处理逻辑
     */
    private Object invokeFailRateLimiterMethod(Method method, Object[] args) throws Exception{
        logger.info("execute {} fail rate limiter strategy...", rateLimiterFailStrategy);
        switch (rateLimiterFailStrategy){
            case RpcConstants.RATE_LIMILTER_FAIL_STRATEGY_EXCEPTION:
            case RpcConstants.RATE_LIMILTER_FAIL_STRATEGY_FALLBACK:
                return this.getFallbackResult(method, args);
            case RpcConstants.RATE_LIMILTER_FAIL_STRATEGY_DIRECT:
                return this.invokeSendRequestMethodWithFusing(method, args);
        }
        return this.invokeSendRequestMethodWithFusing(method, args);
    }

    /**
     * 开启熔断策略时调用的方法
     */
    private Object invokeFusingSendRequestMethod(Method method, Object[] args) throws Exception{
        //如果触发了熔断的规则，则直接返回降级处理数据
        if (fusingInvoker.invokeFusingStrategy()) {
            return this.getFallbackResult(method, args);
        }
        //请求计数加1
        fusingInvoker.incrementCount();

        Object result = null;
        try {
            result = invokeSendRequestMethod(method, args);
            fusingInvoker.markSuccess();
        } catch (Throwable e) {
            exceptionPostProcessor.postExceptionProcessor(e);
            fusingInvoker.markFailed();
            throw new RpcException(e.getMessage());
        }
        return result;
    }


    /**
     * 结合服务熔断请求方法
     */
    private Object invokeSendRequestMethodWithFusing(Method method, Object[] args) throws Exception{
        // 开启了熔断
        if (enableFusing){
            return invokeFusingSendRequestMethod(method, args);
        }else {
            return invokeSendRequestMethod(method, args);
        }
    }

}
