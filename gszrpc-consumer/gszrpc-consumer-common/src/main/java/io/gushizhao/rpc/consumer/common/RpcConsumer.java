package io.gushizhao.rpc.consumer.common;

import io.gushizhao.rpc.common.exception.RpcException;
import io.gushizhao.rpc.common.helper.RpcServiceHelper;
import io.gushizhao.rpc.common.ip.IpUtils;
import io.gushizhao.rpc.constants.RpcConstants;
import io.gushizhao.rpc.consumer.common.handler.RpcConsumerHandler;
import io.gushizhao.rpc.consumer.common.helper.RpcConsumerHandlerHelper;
import io.gushizhao.rpc.consumer.common.initializer.RpcConsumerInitializer;
import io.gushizhao.rpc.consumer.common.manage.ConsumerConnectionManager;
import io.gushizhao.rpc.exception.processor.ExceptionPostProcessor;
import io.gushizhao.rpc.loadbalancer.context.ConnectionsContext;
import io.gushizhao.rpc.processor.FlowPostProcessor;
import io.gushizhao.rpc.protocol.RpcProtocol;
import io.gushizhao.rpc.protocol.meta.ServiceMeta;
import io.gushizhao.rpc.protocol.request.RpcRequest;
import io.gushizhao.rpc.proxy.api.common.ClientThreadPool;
import io.gushizhao.rpc.proxy.api.consumer.Consumer;
import io.gushizhao.rpc.proxy.api.future.RPCFuture;
import io.gushizhao.rpc.registry.api.RegistryService;
import io.gushizhao.rpc.spi.loader.ExtensionLoader;
import io.gushizhao.rpc.threadpool.ConcurrentThreadPool;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
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

    private Bootstrap bootstrap;

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
    // 是否开启直连服务
    private boolean enableDirectServer = false;
    // 直连服务的地址
    private String directServerUrl;
    // 是否开启延迟连接
    private boolean enableDelayConnection = false;
    // 未开启延迟连接时， 是否已经初始化连接
    private volatile boolean initConnection = false;

    private ConcurrentThreadPool concurrentThreadPool;

    private FlowPostProcessor flowPostProcessor;

    private boolean enableBuffer;
    private int bufferSize;

    private ExceptionPostProcessor exceptionPostProcessor;


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
    }

    /**
     * fix04
     * 问题
     * 当以SpringBoot启动服务消费者时，由于通过Spring解析@RpcReference注解的属性会优先于SpringBoot读取yml文件中属性的执行。
     * Spring解析@RpcReference注解的属性后，会调用RpcClient的构造方法，创建RpcClient对象，后续会创建代理对象，启动Netty服务，进而启动服务消费者。
     * 当启动Netty服务时，就会触发group操作。
     * 当SpringBoot读取完yml文件中的属性参数后，会创建RpcClient对象并将其注入Spring的IOC容器中，也会启动Netty服务，此时就会触发两次Netty的group操作。
     *
     * 解决
     * 后续再次通过SpringBoot启动时，按照一定的规则使用yml文件中的属性覆盖@RpcReference注解的属性后，如果不做处理，会无法执行Netty的group操作，此
     * 时，服务消费者启动后，就无法正常实现如下的属性配置优先级。
     * 字段@RpcReference注解属性>yml文件的配置>@RpcReference注解默认属性
     *
     * 对Netty进行group操作时，使用try-catch捕获IllegalStateException异常，在catch代码块中再次执行Netty的group操作，修改后的源码如下所示。
     * @return
     */
    public RpcConsumer buildNettyGroup(){
        try {
            bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                    .handler(new RpcConsumerInitializer(heartbeatInterval, enableBuffer, bufferSize, concurrentThreadPool, flowPostProcessor, exceptionPostProcessor));
        }catch (IllegalStateException e){
            bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                    .handler(new RpcConsumerInitializer(heartbeatInterval, enableBuffer, bufferSize, concurrentThreadPool, flowPostProcessor, exceptionPostProcessor));
        }
        return this;
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

    public RpcConsumer buildConnection(RegistryService registryService) {
        // 未开启延迟连接，并且未初始化连接
        if (!enableDelayConnection && !initConnection) {
            this.initConnection(registryService);
            this.initConnection = true;
        }
        this.startHeartbeat();
        return this;
    }

    private void initConnection(RegistryService registryService) {
        List<ServiceMeta> serviceMetaList = new ArrayList<>();
        try {
            if (enableDirectServer) {
                if (!directServerUrl.contains(RpcConstants.RPC_MULTI_DIRECT_SERVERS_SEPARATOR)) {
                    serviceMetaList.add(this.getDirectServiceMetaWithCheck(directServerUrl));
                } else {
                    serviceMetaList.addAll(this.getMultiServiceMeta(directServerUrl));
                }
            } else {
                serviceMetaList = registryService.discoveryAll();
            }
        } catch (Exception e) {
            logger.error("init connection throws exception, the message is : {}", e.getMessage());
        }
        for (ServiceMeta serviceMeta : serviceMetaList) {
            RpcConsumerHandler handler = null;
            try {
                handler = this.getRpcConsumerHandler(serviceMeta);
            } catch (InterruptedException e) {
                logger.error("call getRpcConsumerHandler() method throws InterruptedException, the message is :{}", e.getMessage());
            }
            RpcConsumerHandlerHelper.put(serviceMeta, handler);
        }
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

        RpcConsumerHandler handler = getRpcConsumerHandlerWithRetry(registryService, serviceKey, invokerHashCode);
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
    private ServiceMeta getServiceMetaWithRetry(RegistryService registryService, String serviceKey, int invokerHashCode) throws Exception {
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

    private RpcConsumerHandler getRpcConsumerHandlerWithRetry(RegistryService registryService, String serviceKey, int invokerHashCode) throws Exception {
        logger.info("获取服务消费者处理器...");
        RpcConsumerHandler handler = getRpcConsumerHandler(registryService, serviceKey, invokerHashCode);
        if (handler == null) {
            for (int i = 1; i <= retryTimes; i++) {
                logger.info("获取服务消费者处理器第【{}】次重试", i);
                handler = getRpcConsumerHandler(registryService, serviceKey, invokerHashCode);
                if (handler != null) {
                    break;
                }
                Thread.sleep(retryInterval);
            }
        }
        return handler;
    }

    private RpcConsumerHandler getRpcConsumerHandler(RegistryService registryService, String serviceKey, int invokerHashCode) throws Exception {
        ServiceMeta serviceMeta = this.getDirectServiceMetaOrWithRetry(registryService, serviceKey, invokerHashCode);
        RpcConsumerHandler handler = null;
        if (serviceMeta != null) {
            handler = getRpcConsumerHandlerWithRetry(serviceMeta);
        }
        return handler;
    }

    private ServiceMeta getDirectServiceMetaOrWithRetry(RegistryService registryService, String serviceKey, int invokerHashCode) throws Exception {
        ServiceMeta serviceMeta = null;
        if (enableDirectServer) {
            serviceMeta = this.getServiceMeta(directServerUrl, registryService, invokerHashCode);
        } else {
            serviceMeta = this.getServiceMetaWithRetry(registryService, serviceKey, invokerHashCode);
        }
        return serviceMeta;
    }

    /**
     * 解析单个服务提供者的地址
     * @return
     */
    private ServiceMeta getDirectServiceMeta(String directServerUrl) {
        ServiceMeta serviceMeta = new ServiceMeta();
        String[] directServerUrlArray = directServerUrl.split(RpcConstants.IP_PORT_SPLIT);
        serviceMeta.setServiceAddr(directServerUrlArray[0].trim());
        serviceMeta.setServicePort(Integer.parseInt(directServerUrlArray[1].trim()));
        return serviceMeta;
    }

    /**
     * 直连某个服务提供者
     * @return
     */
    private ServiceMeta getDirectServiceMetaWithCheck(String directServerUrl) {
        if (StringUtils.isEmpty(directServerUrl)) {
            throw new RpcException("direct server url is null...");
        }
        if (!directServerUrl.contains(RpcConstants.IP_PORT_SPLIT)) {
            throw new RpcException("direct server url not contains :");
        }
        return this.getDirectServiceMeta(directServerUrl);
    }
    /**
     * 处理服务消费者直连多个服务提供者的逻辑
     * @return
     */
    private List<ServiceMeta> getMultiServiceMeta(String directServerUrl) {
        List<ServiceMeta> serviceMetaList = new ArrayList<>();
        String[] directServerUrlArray = directServerUrl.split(RpcConstants.RPC_MULTI_DIRECT_SERVERS_SEPARATOR);
        if (directServerUrlArray != null && directServerUrlArray.length > 0) {
            for (String directUrl : directServerUrlArray) {
                serviceMetaList.add(getDirectServiceMeta(directUrl));
            }
        }
        return serviceMetaList;
    }


    /**
     * 直连某个服务提供者
     * @return
     */
    private ServiceMeta getServiceMeta(String directServerUrl, RegistryService registryService, int invokerHashCode) {
        logger.info("服务消费者直连服务提供者...");
        // 只配置了一个服务提供者地址
        if (!directServerUrl.contains(RpcConstants.RPC_MULTI_DIRECT_SERVERS_SEPARATOR)) {
            return getDirectServiceMetaWithCheck(directServerUrl);
        }
        // 配置了多个服务提供者地址
        return registryService.select(this.getMultiServiceMeta(directServerUrl), invokerHashCode, localIp);
    }


    /**
     * 直连某个服务提供者
     * @return
     */
    private ServiceMeta getDirectServiceMeta() {
        if (StringUtils.isEmpty(directServerUrl)) {
            throw new RpcException("direct server url is null...");
        }
        if (!directServerUrl.contains(RpcConstants.IP_PORT_SPLIT)) {
            throw new RpcException("direct server url not contains :");
        }
        logger.info("服务消费者直连服务提供者===>>> {}", directServerUrl);
        ServiceMeta serviceMeta = new ServiceMeta();
        String[] directServerUrlArray = directServerUrl.split(RpcConstants.IP_PORT_SPLIT);
        serviceMeta.setServiceAddr(directServerUrlArray[0]);
        serviceMeta.setServicePort(Integer.parseInt(directServerUrlArray[1]));
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



    public RpcConsumer setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
        return this;
    }

    public RpcConsumer setScanNotActiveChannelInterval(int scanNotActiveChannelInterval) {
        this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
        return this;
    }

    public RpcConsumer setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
        return this;
    }

    public RpcConsumer setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
        return this;
    }

    public RpcConsumer setEnableDirectServer(boolean enableDirectServer) {
        this.enableDirectServer = enableDirectServer;
        return this;
    }

    public RpcConsumer setDirectServerUrl(String directServerUrl) {
        this.directServerUrl = directServerUrl;
        return this;
    }

    public RpcConsumer setEnableDelayConnection(boolean enableDelayConnection) {
        this.enableDelayConnection = enableDelayConnection;
        return this;
    }

    public RpcConsumer setConcurrentThreadPool(ConcurrentThreadPool concurrentThreadPool) {
        this.concurrentThreadPool = concurrentThreadPool;
        return this;
    }

    public RpcConsumer setFlowPostProcessor(String flowType) {
        if (StringUtils.isEmpty(flowType)) {
            flowType = RpcConstants.FLOW_POST_PROCESSOR_PRINT;
        }
        this.flowPostProcessor = ExtensionLoader.getExtension(FlowPostProcessor.class ,flowType);
        return this;
    }

    public RpcConsumer setEnableBuffer(boolean enableBuffer) {
        this.enableBuffer = enableBuffer;
        return this;
    }

    public RpcConsumer setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    public RpcConsumer setExceptionPostProcessor(String exceptionPostProcessorType) {
        if (StringUtils.isEmpty(exceptionPostProcessorType)) {
            exceptionPostProcessorType = RpcConstants.EXCEPTION_POST_PROCESSOR_PRINT;
        }
        this.exceptionPostProcessor = ExtensionLoader.getExtension(ExceptionPostProcessor.class, exceptionPostProcessorType);
        return this;
    }
}
