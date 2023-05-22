package io.gushizhao.rpc.provider.common.manage;

import io.gushizhao.rpc.constants.RpcConstants;
import io.gushizhao.rpc.protocol.RpcProtocol;
import io.gushizhao.rpc.protocol.enumeration.RpcType;
import io.gushizhao.rpc.protocol.header.RpcHeader;
import io.gushizhao.rpc.protocol.header.RpcHeaderFactory;
import io.gushizhao.rpc.protocol.request.RpcRequest;
import io.gushizhao.rpc.protocol.response.RpcResponse;
import io.gushizhao.rpc.provider.common.cache.ProviderChannelCache;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/15 15:22
 */
public class ProviderConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(ProviderConnectionManager.class);

    /**
     * 扫描滨移除不活跃的连接
     */
    public static void scanNotActiveChannel() {
        Set<Channel> channelCache = ProviderChannelCache.getChannelCache();
        if (channelCache == null || channelCache.isEmpty()) {
            return;
        }
        channelCache.stream().forEach(channel -> {
            if (!channel.isOpen() || !channel.isActive()) {
                channel.close();
                ProviderChannelCache.remove(channel);
            }
        });
    }

    /**
     * 发送ping信息
     */
    public static void broadcastPingMessageFromProvider() {
        Set<Channel> channelCache = ProviderChannelCache.getChannelCache();
        if (channelCache == null || channelCache.isEmpty()) {
            return;
        }
        RpcHeader header = RpcHeaderFactory.getRequestHeader(RpcConstants.SERIALIZATION_PROTOSTUFF, RpcType.HEARTBEAT_FROM_PROVIDER.getType());
        RpcProtocol<RpcResponse> responseRpcProtocol = new RpcProtocol<>();
        RpcResponse response = new RpcResponse();
        response.setResult(new Object[]{RpcConstants.HEARTBEAT_PING});
        responseRpcProtocol.setHeader(header);
        responseRpcProtocol.setBody(response);
        channelCache.stream().forEach(channel -> {
            if (channel.isOpen() && channel.isActive()) {
                logger.info("send heartbeat message to service consumer, the consumer is: {}, the heartbeat message is: {}", channel.remoteAddress(), response.getResult());
                channel.writeAndFlush(responseRpcProtocol);
            }
        });
    }
}
