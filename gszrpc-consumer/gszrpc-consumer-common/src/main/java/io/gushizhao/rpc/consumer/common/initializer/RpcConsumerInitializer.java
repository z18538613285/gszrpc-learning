package io.gushizhao.rpc.consumer.common.initializer;

import io.gushizhao.rpc.codec.RpcDecoder;
import io.gushizhao.rpc.codec.RpcEncoder;
import io.gushizhao.rpc.constants.RpcConstants;
import io.gushizhao.rpc.consumer.common.handler.RpcConsumerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/4/25 15:57
 */
public class RpcConsumerInitializer extends ChannelInitializer<SocketChannel> {

    // 心跳间隔时间，默认30秒
    private int heartbeatInterval = 30000;

    public RpcConsumerInitializer(int heartbeatInterval) {
        if (heartbeatInterval > 0) {
            this.heartbeatInterval = heartbeatInterval;
        }
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(RpcConstants.CODEC_ENCODER, new RpcEncoder());
        pipeline.addLast(RpcConstants.CODEC_DECODER, new RpcDecoder());
        pipeline.addLast(RpcConstants.CODEC_CLIENT_IDLE_HANDLER, new IdleStateHandler(heartbeatInterval, 0 ,0, TimeUnit.MILLISECONDS));
        pipeline.addLast(RpcConstants.CODEC_HANDLER, new RpcConsumerHandler());
    }
}
