package io.gushizhao.rpc.consumer.common.handler;

import com.alibaba.fastjson.JSONObject;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import io.gushizhao.rpc.buffer.cache.BufferCacheManager;
import io.gushizhao.rpc.constants.RpcConstants;
import io.gushizhao.rpc.consumer.common.cache.ConsumerChannelCache;
import io.gushizhao.rpc.consumer.common.context.RpcContext;
import io.gushizhao.rpc.exception.processor.ExceptionPostProcessor;
import io.gushizhao.rpc.protocol.RpcProtocol;
import io.gushizhao.rpc.protocol.enumeration.RpcStatus;
import io.gushizhao.rpc.protocol.enumeration.RpcType;
import io.gushizhao.rpc.protocol.header.RpcHeader;
import io.gushizhao.rpc.protocol.header.RpcHeaderFactory;
import io.gushizhao.rpc.protocol.request.RpcRequest;
import io.gushizhao.rpc.protocol.response.RpcResponse;
import io.gushizhao.rpc.proxy.api.future.RPCFuture;
import io.gushizhao.rpc.threadpool.BufferCacheThreadPool;
import io.gushizhao.rpc.threadpool.ConcurrentThreadPool;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author huzhichao
 * @Description 消费者处理器
 * @Date 2023/4/25 15:46
 */
public class RpcConsumerHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {

    private final Logger logger = LoggerFactory.getLogger(RpcConsumerHandler.class);

    private volatile Channel channel;

    private SocketAddress remotePeer;

    // 存储请求 ID 与 RpcResponse 协议的映射关系
    private Map<Long, RPCFuture> pendingRPC = new ConcurrentHashMap<>();
    //private Map<Long, RpcProtocol<RpcResponse>> pendingResponse = new ConcurrentHashMap<>();

    private ConcurrentThreadPool concurrentThreadPool;

    /**
     * 是否开启缓冲区
     */
    private boolean enableBuffer;

    /**
     * 缓冲区管理器
     */
    private BufferCacheManager<RpcProtocol<RpcResponse>> bufferCacheManager;

    private ExceptionPostProcessor exceptionPostProcessor;

    public RpcConsumerHandler(boolean enableBuffer, int bufferSize, ConcurrentThreadPool concurrentThreadPool, ExceptionPostProcessor exceptionPostProcessor) {
        this.concurrentThreadPool = concurrentThreadPool;
        this.exceptionPostProcessor = exceptionPostProcessor;
        this.enableBuffer = enableBuffer;
        if (enableBuffer){
            this.bufferCacheManager = BufferCacheManager.getInstance(bufferSize);
            BufferCacheThreadPool.submit(() -> {
                consumerBufferCache();
            });
        }
    }


    public Channel getChannel() {
        return channel;
    }

    public SocketAddress getRemotePeer() {
        return remotePeer;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.remotePeer = this.channel.remoteAddress();
        ConsumerChannelCache.add(channel);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        ConsumerChannelCache.remove(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ConsumerChannelCache.remove(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcProtocol<RpcResponse> protocol) throws Exception {
        if (protocol == null) {
            return;
        }
        concurrentThreadPool.submit(() -> {
            logger.info("服务消费者接收到的数据===>>>{}", JSONObject.toJSONString(protocol));
            this.handlerMessage(protocol, channelHandlerContext.channel());
        });

    }

    private void handlerMessage(RpcProtocol<RpcResponse> protocol, Channel channel) {
        RpcHeader header = protocol.getHeader();
        // 心跳消息
        if (header.getMsgType() == (byte) RpcType.HEARTBEAT_TO_CONSUMER.getType()) {
            this.handlerHeartbeatMessageToConsumer(protocol);
        } else if (header.getMsgType() == (byte) RpcType.RESPONSE.getType()) {
            // 请求消息
            this.handlerResponseMessageOrBuffer(protocol);
        } else if (header.getMsgType() == (byte) RpcType.HEARTBEAT_FROM_PROVIDER.getType()) {
            // 请求消息
            this.handlerHeartbeatMessageFromProvider(protocol, channel);
        }
    }

    private void handlerResponseMessage(RpcProtocol<RpcResponse> protocol) {
        RpcHeader header = protocol.getHeader();
        long requestId = header.getRequestId();
        RPCFuture rpcFuture = pendingRPC.remove(requestId);
        if (rpcFuture != null) {
            rpcFuture.done(protocol);
        }
    }

    /**
     * 主要是处理心跳消息
     * @param protocol
     */
    private void handlerHeartbeatMessageToConsumer(RpcProtocol<RpcResponse> protocol) {
        // 此处简单打印即可，实际场景可不做处理
        logger.info("receive service provider heartbeat message, the provider is: {}, the heartbeat message is: {}",
                channel.remoteAddress(), protocol.getBody().getResult());
    }

    /**
     * 主要是处理心跳消息
     * @param protocol
     */
    private void handlerHeartbeatMessageFromProvider(RpcProtocol<RpcResponse> protocol, Channel channel) {
        RpcHeader header = protocol.getHeader();
        header.setMsgType((byte) RpcType.HEARTBEAT_TO_PROVIDER.getType());
        RpcProtocol<RpcRequest> requestRpcProtocol = new RpcProtocol<>();
        RpcRequest request = new RpcRequest();
        request.setParameters(new Object[]{RpcConstants.HEARTBEAT_PONG});
        header.setStatus((byte) RpcStatus.SUCESS.getCode());
        requestRpcProtocol.setHeader(header);
        requestRpcProtocol.setBody(request);
        channel.writeAndFlush(requestRpcProtocol);
    }

    public RPCFuture sendRequest(RpcProtocol<RpcRequest> protocol, boolean async, boolean oneway) {
        return concurrentThreadPool.submit(() -> {
            logger.info("服务消费者发送的数据===>>>{}", JSONObject.toJSONString(protocol));
            return oneway ? this.sendRequestOneway(protocol) : async ? sendRequestASync(protocol) : this.sendRequestSync(protocol);
        });
    }

    // 同步调用
    public RPCFuture sendRequestSync(RpcProtocol<RpcRequest> protocol) {
        RPCFuture rpcFuture = this.getRpcFuture(protocol);
        channel.writeAndFlush(protocol);
        return rpcFuture;
    }
    // 异步调用
    public RPCFuture sendRequestASync(RpcProtocol<RpcRequest> protocol) {
        RPCFuture rpcFuture = this.getRpcFuture(protocol);
        // 如果是异步，则将 RPCFuture 放入RpcContext
        RpcContext.getContext().setRpcFuture(rpcFuture);
        channel.writeAndFlush(protocol);
        return null;
    }

    // 单向调用
    public RPCFuture sendRequestOneway(RpcProtocol<RpcRequest> protocol) {
        channel.writeAndFlush(protocol);
        return null;
    }

    private RPCFuture getRpcFuture(RpcProtocol<RpcRequest> protocol) {
        RPCFuture rpcFuture = new RPCFuture(protocol, concurrentThreadPool);
        RpcHeader header = protocol.getHeader();
        long requestId = header.getRequestId();
        pendingRPC.put(requestId, rpcFuture);
        return rpcFuture;
    }

    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        ConsumerChannelCache.remove(channel);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            // 发送一次心跳数据
            RpcHeader header = RpcHeaderFactory.getRequestHeader(RpcConstants.SERIALIZATION_PROTOSTUFF, RpcType.HEARTBEAT_FROM_CONSUMER.getType());
            RpcProtocol<RpcRequest> requestRpcProtocol = new RpcProtocol<>();
            RpcRequest rpcRequest = new RpcRequest();
            rpcRequest.setParameters(new Object[]{RpcConstants.HEARTBEAT_PING});
            requestRpcProtocol.setHeader(header);
            requestRpcProtocol.setBody(rpcRequest);
            ctx.writeAndFlush(requestRpcProtocol);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }


    /**
     * 消费缓冲区数据
     */
    private void consumerBufferCache() {
        //不断消息缓冲区的数据
        while (true){
            RpcProtocol<RpcResponse> protocol = this.bufferCacheManager.take();
            if (protocol != null){
                this.handlerResponseMessage(protocol);
            }
        }
    }

    /**
     * 包含是否开启了缓冲区的响应消息
     */
    private void handlerResponseMessageOrBuffer(RpcProtocol<RpcResponse> protocol){
        if (enableBuffer){
            logger.info("enable buffer...");
            this.bufferCacheManager.put(protocol);
        }else {
            this.handlerResponseMessage(protocol);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        exceptionPostProcessor.postExceptionProcessor(cause);
        super.exceptionCaught(ctx, cause);
    }
}
