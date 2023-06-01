package io.gushizhao.rpc.provider.common.handler;


import com.alibaba.fastjson.JSONObject;
import com.sun.org.apache.regexp.internal.RE;
import io.gushizhao.rpc.buffer.cache.BufferCacheManager;
import io.gushizhao.rpc.buffer.object.BufferObject;
import io.gushizhao.rpc.cache.result.CacheResultKey;
import io.gushizhao.rpc.cache.result.CacheResultManager;
import io.gushizhao.rpc.common.exception.RpcException;
import io.gushizhao.rpc.common.helper.RpcServiceHelper;
import io.gushizhao.rpc.common.threadpool.ServerThreadPool;
import io.gushizhao.rpc.connection.manager.ConnectionManager;
import io.gushizhao.rpc.constants.RpcConstants;
import io.gushizhao.rpc.exception.processor.ExceptionPostProcessor;
import io.gushizhao.rpc.fusing.api.FusingInvoker;
import io.gushizhao.rpc.protocol.RpcProtocol;
import io.gushizhao.rpc.protocol.enumeration.RpcStatus;
import io.gushizhao.rpc.protocol.enumeration.RpcType;
import io.gushizhao.rpc.protocol.header.RpcHeader;
import io.gushizhao.rpc.protocol.request.RpcRequest;
import io.gushizhao.rpc.protocol.response.RpcResponse;
import io.gushizhao.rpc.provider.common.cache.ProviderChannelCache;
import io.gushizhao.rpc.ratelimiter.api.RateLimiterInvoker;
import io.gushizhao.rpc.reflect.api.ReflectInvoker;
import io.gushizhao.rpc.spi.loader.ExtensionLoader;
import io.gushizhao.rpc.threadpool.BufferCacheThreadPool;
import io.gushizhao.rpc.threadpool.ConcurrentThreadPool;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @Author huzhichao
 * @Description 实现 netty 中的SimpleChannelInboundHandler 类实现消息的收发功能
 * @Date 2023/4/23 11:32
 */
public class RpcProviderHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {

    private final Logger logger = LoggerFactory.getLogger(RpcProviderHandler.class);

    private ReflectInvoker reflectInvoker;

    private final Map<String, Object> handlerMap;

    private final boolean enableResultCache;

    private final CacheResultManager<RpcProtocol<RpcResponse>> cacheResultManager;

    private final ConcurrentThreadPool concurrentThreadPool;

    private ConnectionManager connectionManager;
    /**
     * 是否开启缓冲区
     */
    private boolean enableBuffer;

    /**
     * 缓冲区管理器
     */
    private BufferCacheManager<BufferObject<RpcRequest>> bufferCacheManager;

    /**
     * 是否开启限流
     */
    private boolean enableRateLimiter;

    /**
     * 限流SPI接口
     */
    private RateLimiterInvoker rateLimiterInvoker;

    private String rateLimiterFailStrategy;

    private boolean enableFusing;

    private FusingInvoker fusingInvoker;

    private ExceptionPostProcessor exceptionPostProcessor;


    public RpcProviderHandler(String reflectType, boolean enableResultCache, int resultCacheExpire, int corePoolSize, int maximumPoolSize,
                              int maxConnections, String disuseStrategyType, boolean enableBuffer, int bufferSize, boolean enableRateLimiter,
                              String rateLimiterType, int permits, int milliSeconds, String rateLimiterFailStrategy, boolean enableFusing,
                              String fusingType, double totalFailure, int fusingMilliSeconds, String exceptionPostProcessorType, Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
        this.reflectInvoker = ExtensionLoader.getExtension(ReflectInvoker.class, reflectType);
        if (resultCacheExpire <= 0) {
            resultCacheExpire = RpcConstants.RPC_SCAN_RESULT_CACHE_EXPIRE;
        }
        this.enableResultCache = enableResultCache;
        this.cacheResultManager = CacheResultManager.getInstance(resultCacheExpire, enableResultCache);
        this.concurrentThreadPool = ConcurrentThreadPool.getInstance(corePoolSize, maximumPoolSize);
        this.connectionManager = ConnectionManager.getInstance(maxConnections, disuseStrategyType);
        this.enableBuffer = enableBuffer;
        //开启缓冲
        this.initBuffer(bufferSize);
        this.enableRateLimiter = enableRateLimiter;
        this.initRateLimiter(rateLimiterType, permits, milliSeconds);
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
     * 初始化缓冲区数据
     */
    private void initBuffer(int bufferSize) {
        //开启缓冲
        if (enableBuffer) {
            logger.info("enable buffer...");
            bufferCacheManager = BufferCacheManager.getInstance(bufferSize);
            BufferCacheThreadPool.submit(() -> {
                consumerBufferCache();
            });
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

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ProviderChannelCache.add(ctx.channel());
        connectionManager.add(ctx.channel());
    }


    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        ProviderChannelCache.remove(ctx.channel());
        connectionManager.remove(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ProviderChannelCache.remove(ctx.channel());
        connectionManager.remove(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcProtocol<RpcRequest> protocol) throws Exception {
        concurrentThreadPool.submit(() -> {
            connectionManager.update(channelHandlerContext.channel());
            if (enableBuffer) {  //开启队列缓冲
                this.bufferRequest(channelHandlerContext, protocol);
            } else {  //未开启队列缓冲
                this.submitRequest(channelHandlerContext, protocol);
            }
        });

    }

    private RpcProtocol<RpcResponse> handlerMessage(RpcProtocol<RpcRequest> protocol, Channel channel) {
        RpcProtocol<RpcResponse> responseRpcProtocol = null;
        RpcHeader header = protocol.getHeader();
        // 心跳消息
        if (header.getMsgType() == (byte) RpcType.HEARTBEAT_FROM_CONSUMER.getType()) {
            responseRpcProtocol = handlerHeartbeatMessageFromConsumer(protocol, header);
        } else if (header.getMsgType() == (byte) RpcType.REQUEST.getType()) {
            // 请求消息
            responseRpcProtocol = handlerRequestMessageWithCacheAndRateLimiter(protocol, header);
        } else if (header.getMsgType() == (byte) RpcType.HEARTBEAT_TO_PROVIDER.getType()) {
            // 请求消息
            handlerHeartbeatMessageToProvider(protocol, channel);
        }
        return responseRpcProtocol;
    }

    /**
     * 主要是处理服务消费者发送过来的请求消息
     *
     * @param protocol
     * @param header
     * @return
     */
    private RpcProtocol<RpcResponse> handlerRequestMessage(RpcProtocol<RpcRequest> protocol, RpcHeader header) {
        RpcRequest request = protocol.getBody();
        logger.debug("Receive request " + header.getRequestId());

        RpcProtocol<RpcResponse> responseRpcProtocol = new RpcProtocol<>();
        RpcResponse response = new RpcResponse();
        try {
            Object result = handle(request);
            response.setResult(result);
            response.setAsync(request.getAsync());
            response.setOneway(request.getOneway());
            header.setStatus((byte) RpcStatus.SUCESS.getCode());
        } catch (Throwable t) {
            exceptionPostProcessor.postExceptionProcessor(t);
            response.setError(t.toString());
            header.setStatus((byte) RpcStatus.FAIL.getCode());
            logger.error("RPC Server handle request error", t);
            throw new RpcException(t);
        }

        responseRpcProtocol.setHeader(header);
        responseRpcProtocol.setBody(response);
        return responseRpcProtocol;
    }

    /**
     * 主要是处理消费者发送过来的心跳消息，按照自定义网络传输协议，将消息封装成 pong 消息返回给服务消费者
     *
     * @param protocol
     * @param header
     * @return
     */
    private RpcProtocol<RpcResponse> handlerHeartbeatMessageFromConsumer(RpcProtocol<RpcRequest> protocol, RpcHeader header) {
        header.setMsgType((byte) RpcType.HEARTBEAT_TO_CONSUMER.getType());
        RpcRequest request = protocol.getBody();
        RpcProtocol<RpcResponse> responseRpcProtocol = new RpcProtocol<>();
        RpcResponse response = new RpcResponse();
        response.setResult(RpcConstants.HEARTBEAT_PONG);
        response.setAsync(request.getAsync());
        response.setOneway(request.getOneway());
        header.setStatus((byte) RpcStatus.SUCESS.getCode());
        responseRpcProtocol.setHeader(header);
        responseRpcProtocol.setBody(response);
        return responseRpcProtocol;
    }

    /**
     * 主要是处理消费者发送过来的心跳消息，按照自定义网络传输协议，将消息封装成 pong 消息返回给服务消费者
     *
     * @param protocol
     * @return
     */
    private void handlerHeartbeatMessageToProvider(RpcProtocol<RpcRequest> protocol, Channel channel) {
        logger.info("receive service consumer heartbeat message, the consumer is: {}, the heartbeat message is: {}",
                channel.remoteAddress(), protocol.getBody().getParameters()[0]);
    }

    private Object handle(RpcRequest request) throws Throwable {
        String serviceKey = RpcServiceHelper.buildServiceKey(request.getClassName(), request.getVersion(), request.getGroup());
        Object serviceBean = handlerMap.get(serviceKey);
        if (serviceBean == null) {
            throw new RuntimeException(String.format("service not exist: %s:%s", request.getClassName(), request.getMethodName()));
        }
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        logger.debug(serviceClass.getName());
        logger.debug(methodName);
        if (parameterTypes != null && parameterTypes.length > 0) {
            for (int i = 0; i < parameterTypes.length; i++) {
                logger.debug(parameterTypes[i].getName());
            }
        }
        if (parameters != null && parameters.length > 0) {
            for (int i = 0; i < parameters.length; i++) {
                logger.debug(parameters[i].toString());
            }
        }
        return this.reflectInvoker.invokeMethod(serviceBean, serviceClass, methodName, parameterTypes, parameters);
    }

/*

    // 利用反射技术调用方法
    private Object invokeMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Throwable{
        switch (this.reflectType) {
            case RpcConstants.REFLECT_TYPE_JDK:
                return this.invokeJDKMethod(serviceBean, serviceClass, methodName, parameterTypes, parameters);
            case RpcConstants.REFLECT_TYPE_CGLIB:
                return this.invokeCGLIBMethod(serviceBean, serviceClass, methodName, parameterTypes, parameters);
            default:
                throw new IllegalArgumentException("not support reflect type");
        }
    }

    private Object invokeCGLIBMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters)  throws Throwable{
        // Cglib reflect
        logger.info("use cglib reflect type invoke method...");
        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
        return serviceFastMethod.invoke(serviceBean, parameters);

    }
*/

    private Object invokeJDKMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Throwable {
        // JDK reflect
        logger.info("use jdk reflect type invoke method...");
        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(serviceBean, parameters);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 如果是IdleStateEvent时间
        if (evt instanceof IdleStateEvent) {
            Channel channel = ctx.channel();
            try {
                logger.info("IdleStateEvent triggered, close channel " + channel);
                connectionManager.remove(ctx.channel());
                channel.close();
            } finally {
                channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    private RpcProtocol<RpcResponse> handlerRequestMessageWithCache(RpcProtocol<RpcRequest> protocol, RpcHeader header) {
        header.setMsgType((byte) RpcType.RESPONSE.getType());
        if (enableResultCache) {
            return handlerRequestMessageCache(protocol, header);
        }
        return handlerRequestMessageWithFusing(protocol, header);
    }

    /**
     * 先村缓存中获取结果数据，没有则调用服务提供者真实方法
     *
     * @param protocol
     * @param header
     * @return
     */
    private RpcProtocol<RpcResponse> handlerRequestMessageCache(RpcProtocol<RpcRequest> protocol, RpcHeader header) {
        RpcRequest request = protocol.getBody();
        CacheResultKey cacheKey = new CacheResultKey(request.getClassName(), request.getMethodName(), request.getParameterTypes(), request.getParameters(), request.getVersion(), request.getGroup());
        RpcProtocol<RpcResponse> responseRpcProtocol = cacheResultManager.get(cacheKey);
        if (responseRpcProtocol == null) {
            responseRpcProtocol = handlerRequestMessageWithFusing(protocol, header);
            cacheKey.setCacheTimeStamp(System.currentTimeMillis());
            cacheResultManager.put(cacheKey, responseRpcProtocol);
        }
        responseRpcProtocol.setHeader(header);
        return responseRpcProtocol;
    }

    /**
     * 消费缓冲区的数据
     */
    private void consumerBufferCache() {
        //不断消息缓冲区的数据
        while (true) {
            BufferObject<RpcRequest> bufferObject = this.bufferCacheManager.take();
            if (bufferObject != null) {
                ChannelHandlerContext ctx = bufferObject.getCtx();
                RpcProtocol<RpcRequest> protocol = bufferObject.getProtocol();
                RpcHeader header = protocol.getHeader();
                RpcProtocol<RpcResponse> responseRpcProtocol = handlerRequestMessageWithCache(protocol, header);
                this.writeAndFlush(header.getRequestId(), ctx, responseRpcProtocol);
            }
        }
    }


    /**
     * 向服务消费者写回数据
     */
    private void writeAndFlush(long requestId, ChannelHandlerContext ctx, RpcProtocol<RpcResponse> responseRpcProtocol) {
        ctx.writeAndFlush(responseRpcProtocol).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                logger.debug("Send response for request " + requestId);
            }
        });
    }

    /**
     * 提交请求
     */
    private void submitRequest(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) {
        RpcProtocol<RpcResponse> responseRpcProtocol = handlerMessage(protocol, ctx.channel());
        writeAndFlush(protocol.getHeader().getRequestId(), ctx, responseRpcProtocol);
    }

    /**
     * 缓冲数据
     */
    private void bufferRequest(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) {
        RpcHeader header = protocol.getHeader();
        //接收到服务消费者发送的心跳消息
        if (header.getMsgType() == (byte) RpcType.HEARTBEAT_FROM_CONSUMER.getType()) {
            RpcProtocol<RpcResponse> responseRpcProtocol = handlerHeartbeatMessageFromConsumer(protocol, header);
            this.writeAndFlush(protocol.getHeader().getRequestId(), ctx, responseRpcProtocol);
        } else if (header.getMsgType() == (byte) RpcType.HEARTBEAT_TO_PROVIDER.getType()) {  //接收到服务消费者响应的心跳消息
            handlerHeartbeatMessageToProvider(protocol, ctx.channel());
        } else if (header.getMsgType() == (byte) RpcType.REQUEST.getType()) { //请求消息
            this.bufferCacheManager.put(new BufferObject<>(ctx, protocol));
        }
    }


    /**
     * 带有限流模式提交请求信息
     */
    private RpcProtocol<RpcResponse> handlerRequestMessageWithCacheAndRateLimiter(RpcProtocol<RpcRequest> protocol, RpcHeader header) {
        RpcProtocol<RpcResponse> responseRpcProtocol = null;
        if (enableRateLimiter) {
            if (rateLimiterInvoker.tryAcquire()) {
                try {
                    responseRpcProtocol = this.handlerRequestMessageWithCache(protocol, header);
                } finally {
                    rateLimiterInvoker.release();
                }
            } else {
                // TODO 执行各种策略
                responseRpcProtocol = this.invokeFailRateLimiterMethod(protocol, header);
            }
        } else {
            responseRpcProtocol = this.handlerRequestMessageWithCache(protocol, header);
        }
        return responseRpcProtocol;
    }


    /**
     * 执行限流失败时的处理逻辑
     */
    private RpcProtocol<RpcResponse> invokeFailRateLimiterMethod(RpcProtocol<RpcRequest> protocol, RpcHeader header) {
        logger.info("execute {} fail rate limiter strategy...", rateLimiterFailStrategy);
        switch (rateLimiterFailStrategy) {
            case RpcConstants.RATE_LIMILTER_FAIL_STRATEGY_EXCEPTION:
            case RpcConstants.RATE_LIMILTER_FAIL_STRATEGY_FALLBACK:
                return this.handlerFallbackMessage(protocol);
            case RpcConstants.RATE_LIMILTER_FAIL_STRATEGY_DIRECT:
                return this.handlerRequestMessageWithCache(protocol, header);
        }
        return this.handlerRequestMessageWithCache(protocol, header);
    }


    /**
     * 处理降级（容错）消息
     */
    private RpcProtocol<RpcResponse> handlerFallbackMessage(RpcProtocol<RpcRequest> protocol) {
        RpcProtocol<RpcResponse> responseRpcProtocol = new RpcProtocol<>();
        RpcHeader header = protocol.getHeader();
        header.setStatus((byte) RpcStatus.FAIL.getCode());
        header.setMsgType((byte) RpcType.RESPONSE.getType());
        responseRpcProtocol.setHeader(header);

        RpcResponse response = new RpcResponse();
        response.setError("provider execute ratelimiter fallback strategy...");
        responseRpcProtocol.setBody(response);

        return responseRpcProtocol;
    }

    /**
     * 开启熔断策略时调用的方法
     */
    private RpcProtocol<RpcResponse> handlerFusingRequestMessage(RpcProtocol<RpcRequest> protocol, RpcHeader header) {
        //如果触发了熔断的规则，则直接返回降级处理数据
        if (fusingInvoker.invokeFusingStrategy()) {
            return handlerFallbackMessage(protocol);
        }
        //请求计数加1
        fusingInvoker.incrementCount();

        //调用handlerRequestMessage()方法获取数据
        RpcProtocol<RpcResponse> responseRpcProtocol = handlerRequestMessage(protocol, header);
        if (responseRpcProtocol == null) {
            return null;
        }
        //如果是调用失败，则失败次数加1
        if (responseRpcProtocol.getHeader().getStatus() == (byte) RpcStatus.FAIL.getCode()) {
            fusingInvoker.markFailed();
        } else {
            fusingInvoker.markSuccess();
        }
        return responseRpcProtocol;
    }


    /**
     * 结合服务熔断请求方法
     */
    private RpcProtocol<RpcResponse> handlerRequestMessageWithFusing(RpcProtocol<RpcRequest> protocol, RpcHeader header){
        if (enableFusing){
            return handlerFusingRequestMessage(protocol, header);
        }else {
            return handlerRequestMessage(protocol, header);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("server caught exception", cause);
        exceptionPostProcessor.postExceptionProcessor(cause);
        ProviderChannelCache.remove(ctx.channel());
        connectionManager.remove(ctx.channel());
        ctx.close();
    }
}
