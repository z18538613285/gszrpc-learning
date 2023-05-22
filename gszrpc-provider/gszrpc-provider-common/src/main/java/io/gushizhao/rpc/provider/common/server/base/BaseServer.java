package io.gushizhao.rpc.provider.common.server.base;

import io.gushizhao.rpc.codec.RpcDecoder;
import io.gushizhao.rpc.codec.RpcEncoder;
import io.gushizhao.rpc.constants.RpcConstants;
import io.gushizhao.rpc.provider.common.handler.RpcProviderHandler;
import io.gushizhao.rpc.provider.common.manage.ProviderConnectionManager;
import io.gushizhao.rpc.provider.common.server.api.Server;
import io.gushizhao.rpc.registry.api.RegistryService;
import io.gushizhao.rpc.registry.api.config.RegistryConfig;
import io.gushizhao.rpc.registry.zookeeper.ZookeeperRegistryService;
import io.gushizhao.rpc.spi.loader.ExtensionLoader;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/4/23 11:41
 */
public class BaseServer implements Server {
    private final Logger logger = LoggerFactory.getLogger(BaseServer.class);
    // 主机域名或者 IP地址
    protected String host = "127.0.0.1";
    // 端口号
    protected int port = 27110;
    //存储的是实体类关系
    protected Map<String, Object> handlerMap = new HashMap<>();

    private String reflectType;

    protected RegistryService registryService;

    private ScheduledExecutorService executorService;
    // 心跳间隔时间，默认30秒
    private int heartbeatInterval = 30000;
    // 扫描并移除空闲连接时间，默认60秒
    private int scanNotActiveChannelInterval = 60000;

    public BaseServer(String serverAddress, String registryAddress, String registryType, String registryLoadBalanceType, String reflectType, int heartbeatInterval, int scanNotActiveChannelInterval) {
        if (heartbeatInterval > 0) {
            this.heartbeatInterval = heartbeatInterval;
        }
        if (scanNotActiveChannelInterval > 0) {
            this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
        }
        if (!StringUtils.isEmpty(serverAddress)) {
            String[] serverArray = serverAddress.split(":");
            this.host = serverArray[0];
            this.port = Integer.parseInt(serverArray[1]);
        }
        this.reflectType = reflectType;
        this.registryService = this.getRegistryService(registryAddress, registryType, registryLoadBalanceType);
    }

    /**
     * 主要是创建服务注册与发现的实现类
     *
     * @param registryAddress
     * @param registryType
     * @return
     */
    private RegistryService getRegistryService(String registryAddress, String registryType, String registryLoadBalanceType) {
        // TODO 后续扩展 支持SPI
        RegistryService registryService = null;
        try {
            //registryService = new ZookeeperRegistryService();
            registryService = ExtensionLoader.getExtension(RegistryService.class, registryType);
            registryService.init(new RegistryConfig(registryAddress, registryType, registryLoadBalanceType));
        } catch (Exception e) {
            logger.error("RPC Server init error", e);
        }
        return registryService;
    }

    @Override
    public void startNettyServer() {
        this.startHeartbeat();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(RpcConstants.CODEC_DECODER, new RpcDecoder())
                                    .addLast(RpcConstants.CODEC_ENCODER, new RpcEncoder())
                                    /**
                                     * readerIdleTime : 读空闲超时检测定时任务会在每 readerIdleTime 时间内启动一次，检测在 readerIdleTime 内是否发生过读事件，如果没有发生，
                                     *      则触发读超时事件 READER_IDLE_STATE_EVENT 并将超时事件交给 NettyClientHandler 处理，如果为 0 则不创建定时任务
                                     * writerIdleTime： 与 readerIdleTime 作用类似，只不过该参数定义的是写时间
                                     * allIdleTime：同时检测读事件和写时间，触发超时事件 ALL_IDLE_STATE_EVENT
                                     * unit： 表示前面三个参数的单位，毫秒
                                     */
                                    .addLast(RpcConstants.CODEC_SERVER_IDLE_HANDLER, new IdleStateHandler(0, 0, heartbeatInterval, TimeUnit.MILLISECONDS))
                                    .addLast(RpcConstants.CODEC_HANDLER, new RpcProviderHandler(reflectType, handlerMap));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = bootstrap.bind(host, port).sync();
            logger.info("Server started on {}:{}", host, port);
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error("RPC Server start error", e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }


    private void startHeartbeat() {
        executorService = Executors.newScheduledThreadPool(2);
        // 扫描并处理所有不活动的连接
        executorService.scheduleAtFixedRate(() -> {
            logger.info("================scanNotActiveChannel================");
            ProviderConnectionManager.scanNotActiveChannel();
        }, 10, scanNotActiveChannelInterval, TimeUnit.MILLISECONDS);

        executorService.scheduleAtFixedRate(() -> {
            logger.info("================broadcastPingMessageFromProvider================");
            ProviderConnectionManager.broadcastPingMessageFromProvider();
        }, 3, heartbeatInterval, TimeUnit.MILLISECONDS);
    }
}
