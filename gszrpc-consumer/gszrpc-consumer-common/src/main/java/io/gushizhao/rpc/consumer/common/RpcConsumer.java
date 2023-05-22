package io.gushizhao.rpc.consumer.common;

import io.gushizhao.rpc.common.helper.RpcServiceHelper;
import io.gushizhao.rpc.common.ip.IpUtils;
import io.gushizhao.rpc.constants.RpcConstants;
import io.gushizhao.rpc.consumer.common.handler.RpcConsumerHandler;
import io.gushizhao.rpc.consumer.common.helper.RpcConsumerHandlerHelper;
import io.gushizhao.rpc.consumer.common.initializer.RpcConsumerInitializer;
import io.gushizhao.rpc.consumer.common.manage.ConsumerConnectionManager;
import io.gushizhao.rpc.loadbalancer.context.ConnectionsContext;
import io.gushizhao.rpc.protocol.RpcProtocol;
import io.gushizhao.rpc.protocol.meta.ServiceMeta;
import io.gushizhao.rpc.protocol.request.RpcRequest;
import io.gushizhao.rpc.proxy.api.common.ClientThreadPool;
import io.gushizhao.rpc.proxy.api.consumer.Consumer;
import io.gushizhao.rpc.proxy.api.future.RPCFuture;
import io.gushizhao.rpc.registry.api.RegistryService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/4/25 16:18
 */
public class RpcConsumer implements Consumer {

    private final Logger logger = LoggerFactory.getLogger(RpcConsumer.class);

    private final Bootstrap bootstrap;

    private final EventLoopGroup eventLoopGroup;

    private static volatile RpcConsumer instance;

    private final String localIp;

    private static Map<String, RpcConsumerHandler> handlerMap = new ConcurrentHashMap<>();

    private ScheduledExecutorService executorService;
    // 心跳间隔时间，默认30秒
    private int heartbeatInterval = 30000;
    // 扫描并移除空闲连接时间，默认60秒
    private int scanNotActiveChannelInterval = 60000;
    // 重试间隔时间
    private int retryInterval = 1000;
    // 重试次数
    private int retryTimes = 3;
    // 当前重试次数
    private volatile int currentConnectRetryTimes = 0;

    private RpcConsumer(int heartbeatInterval, int scanNotActiveChannelInterval, int retryInterval, int retryTimes) {
        this.retryInterval = retryInterval <= 0 ? RpcConstants.DEFAULT_RETRY_INTERVAL : retryInterval;
        this.retryTimes = retryTimes <=0 ? RpcConstants.DEFAULT_RETRY_TIMES : retryTimes;
        if (heartbeatInterval > 0) {
            this.heartbeatInterval = heartbeatInterval;
        }
        if (scanNotActiveChannelInterval > 0) {
            this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
        }
        localIp = IpUtils.getLocalHostIp();
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup(4);
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new RpcConsumerInitializer(heartbeatInterval));
        this.startHeartbeat();
    }

    public static RpcConsumer getInstance(int heartbeatInterval, int scanNotActiveChannelInterval, int retryInterval, int retryTimes) {
        if (instance == null) {
            synchronized (RpcConsumer.class) {
                if (instance == null) {
                    instance = new RpcConsumer(heartbeatInterval, scanNotActiveChannelInterval, retryInterval, retryTimes);
                }
            }
        }
        return instance;
    }

    public void close() {
        RpcConsumerHandlerHelper.closeRpcClientHandler();
        eventLoopGroup.shutdownGracefully();
        ClientThreadPool.shutdown();
    }

    @Override
    public RPCFuture sendRequest(RpcProtocol<RpcRequest> protocol, RegistryService registryService) throws Exception {
        RpcRequest request = protocol.getBody();
        String serviceKey = RpcServiceHelper.buildServiceKey(request.getClassName(), request.getVersion(), request.getGroup());
        Object[] parameters = request.getParameters();
        int invokerHashCode = (parameters == null || parameters.length <= 0) ? serviceKey.hashCode() : parameters[0].hashCode();
        ServiceMeta serviceMeta = this.getServiceMeta(registryService, serviceKey, invokerHashCode);

        RpcConsumerHandler handler = null;
        if (serviceMeta != null) {
            handler = getRpcConsumerHandlerWithRetry(serviceMeta);
        }
        RPCFuture rpcFuture = null;
        if (handler != null) {
            rpcFuture = handler.sendRequest(protocol, request.getAsync(), request.getOneway());
        }
        return rpcFuture;
    }

    /**
     * 缓存中获取
     * @param serviceMeta
     * @return
     * @throws InterruptedException
     */
    private RpcConsumerHandler getRpcConsumerHandlerWithCache(ServiceMeta serviceMeta) throws InterruptedException{
        RpcConsumerHandler handler = RpcConsumerHandlerHelper.get(serviceMeta);
        if (handler == null) {
            handler = getRpcConsumerHandler(serviceMeta);
            RpcConsumerHandlerHelper.put(serviceMeta, handler);
        } else if (!handler.getChannel().isActive()) {
            handler.close();
            handler = getRpcConsumerHandler(serviceMeta);
            RpcConsumerHandlerHelper.put(serviceMeta, handler);
        }
        return handler;
    }


        /**
         * 创建连接并返回 RpcClientHandler
         *
         * @return
         */
    private RpcConsumerHandler getRpcConsumerHandler(ServiceMeta serviceMeta) throws InterruptedException{
        ChannelFuture channelFuture = bootstrap.connect(serviceMeta.getServiceAddr(), serviceMeta.getServicePort()).sync();
        channelFuture.addListener((ChannelFutureListener) listener -> {
            if (channelFuture.isSuccess()) {
                logger.info("connect rpc server {} on port {} success.", serviceMeta.getServiceAddr(), serviceMeta.getServicePort());
                // 添加连接信息，在服务消费者端记录每个提供者实例的连接次数
                ConnectionsContext.add(serviceMeta);
                currentConnectRetryTimes = 0;
            } else {
                logger.info("connect rpc server {} on port {} failed.", serviceMeta.getServiceAddr(), serviceMeta.getServicePort());
                channelFuture.cause().printStackTrace();
                eventLoopGroup.shutdownGracefully();
            }
        });

        return channelFuture.channel().pipeline().get(RpcConsumerHandler.class);
    }


    private void startHeartbeat() {
        executorService = Executors.newScheduledThreadPool(2);
        // 扫描并处理所有不活动的连接
        executorService.scheduleAtFixedRate(() -> {
            logger.info("================scanNotActiveChannel================");
            ConsumerConnectionManager.scanNotActiveChannel();
        }, 10, scanNotActiveChannelInterval, TimeUnit.MILLISECONDS);

        executorService.scheduleAtFixedRate(() -> {
            logger.info("================broadcastPingMessageFromConsumer================");
            ConsumerConnectionManager.broadcastPingMessageFromConsumer();
        }, 3, heartbeatInterval, TimeUnit.MILLISECONDS);
    }

    /**
     * 获取服务提供者元数据
     * @param registryService
     * @param serviceKey
     * @param invokerHashCode
     * @return
     * @throws Exception
     */
    private ServiceMeta getServiceMeta(RegistryService registryService, String serviceKey, int invokerHashCode) throws Exception {
        // 首次获取服务元数据信息，如果获取到，则直接返回，否则进行重试
        logger.info("获取服务提供者元数据...");
        ServiceMeta serviceMeta = registryService.discovery(serviceKey, invokerHashCode, localIp);
        // 启动重试机制
        if (serviceMeta == null) {
            for (int i = 1; i <= retryTimes; i++) {
                logger.info("获取服务提供者元数据第【{}】次重试...", i);
                serviceMeta = registryService.discovery(serviceKey, invokerHashCode, localIp);
                if (serviceMeta != null) {
                    break;
                }
                Thread.sleep(retryInterval);
            }
        }
        return serviceMeta;
    }

    private RpcConsumerHandler getRpcConsumerHandlerWithRetry(ServiceMeta serviceMeta) throws InterruptedException {
        logger.info("服务消费者连接服务消费者...");
        RpcConsumerHandler handler = null;
        try {
            handler = this.getRpcConsumerHandlerWithCache(serviceMeta);
        } catch (Exception e) {
            // 连接异常
            if (e instanceof ConnectException) {
                // 启动重试机制
                if (handler == null) {
                    if (currentConnectRetryTimes < retryTimes) {
                        currentConnectRetryTimes++;
                        logger.info("服务消费者连接服务提供者第【{}】次重试...", currentConnectRetryTimes);
                        handler = this.getRpcConsumerHandlerWithRetry(serviceMeta);
                        Thread.sleep(retryInterval);
                    }
                }
            }
        }
        return handler;
    }

}
