package io.gushizhao.rpc.consumer.common.manage;

import io.gushizhao.rpc.constants.RpcConstants;
import io.gushizhao.rpc.consumer.common.cache.ConsumerChannelCache;
import io.gushizhao.rpc.protocol.RpcProtocol;
import io.gushizhao.rpc.protocol.enumeration.RpcType;
import io.gushizhao.rpc.protocol.header.RpcHeader;
import io.gushizhao.rpc.protocol.header.RpcHeaderFactory;
import io.gushizhao.rpc.protocol.request.RpcRequest;
import io.netty.channel.Channel;
import javafx.scene.layout.RowConstraints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/15 15:22
 */
public class ConsumerConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerConnectionManager.class);

    /**
     * 扫描滨移除不活跃的连接
     */
    public static void scanNotActiveChannel() {
        Set<Channel> channelCache = ConsumerChannelCache.getChannelCache();
        if (channelCache == null || channelCache.isEmpty()) {
            return;
        }
        channelCache.stream().forEach(channel -> {
            if (!channel.isOpen() || !channel.isActive()) {
                channel.close();
                ConsumerChannelCache.remove(channel);
            }
        });
    }

    /**
     * 发送ping信息
     */
    public static void broadcastPingMessageFromConsumer() {
        Set<Channel> channelCache = ConsumerChannelCache.getChannelCache();
        if (channelCache == null || channelCache.isEmpty()) {
            return;
        }
        RpcHeader header = RpcHeaderFactory.getRequestHeader(RpcConstants.SERIALIZATION_PROTOSTUFF, RpcType.HEARTBEAT_FROM_CONSUMER.getType());
        RpcProtocol<RpcRequest> requestRpcProtocol = new RpcProtocol<>();
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setParameters(new Object[]{RpcConstants.HEARTBEAT_PING});
        requestRpcProtocol.setHeader(header);
        requestRpcProtocol.setBody(rpcRequest);
        channelCache.stream().forEach(channel -> {
            if (channel.isOpen() && channel.isActive()) {
                logger.info("send heartbeat message to service provider, the provider is: {}, the heartbeat message is: {}", channel.remoteAddress(), RpcConstants.HEARTBEAT_PING);
                channel.writeAndFlush(requestRpcProtocol);
            }
        });
    }
}
