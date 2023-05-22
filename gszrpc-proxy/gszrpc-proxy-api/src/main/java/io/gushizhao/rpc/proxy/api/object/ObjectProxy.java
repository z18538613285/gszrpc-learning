package io.gushizhao.rpc.proxy.api.object;

import io.gushizhao.rpc.protocol.RpcProtocol;
import io.gushizhao.rpc.protocol.enumeration.RpcType;
import io.gushizhao.rpc.protocol.header.RpcHeaderFactory;
import io.gushizhao.rpc.protocol.request.RpcRequest;
import io.gushizhao.rpc.proxy.api.async.IAsyncObjectProxy;
import io.gushizhao.rpc.proxy.api.consumer.Consumer;
import io.gushizhao.rpc.proxy.api.future.RPCFuture;
import io.gushizhao.rpc.registry.api.RegistryService;
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
public class ObjectProxy <T> implements IAsyncObjectProxy, InvocationHandler {

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

    public ObjectProxy(Class<T> clazz) {
        this.clazz = clazz;
    }

    public ObjectProxy(Class<T> clazz, String serviceVersion, String serviceGroup, long timeout, RegistryService registryService, Consumer consumer, String serializationType, boolean async, boolean oneway) {
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

        RPCFuture rpcFuture = this.consumer.sendRequest(requestRpcProtocol, registryService);
        return rpcFuture == null ? null : timeout > 0 ? rpcFuture.get(timeout, TimeUnit.MILLISECONDS) : rpcFuture.get();
    }

    @Override
    public RPCFuture call(String funcName, Object... args) {
        RpcProtocol<RpcRequest> request = createRequest(this.clazz.getName(), funcName, args);

        RPCFuture rpcFuture = null;
        try {
            rpcFuture = this.consumer.sendRequest(request, registryService);
        } catch (Exception e) {
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

        for (int i = 0; i <paramterTypes.length; i++) {
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
}
