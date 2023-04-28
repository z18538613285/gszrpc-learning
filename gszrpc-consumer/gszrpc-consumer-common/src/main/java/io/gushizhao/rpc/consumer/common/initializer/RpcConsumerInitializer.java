package io.gushizhao.rpc.consumer.common.initializer;

import io.gushizhao.rpc.codec.RpcDecoder;
import io.gushizhao.rpc.codec.RpcEncoder;
import io.gushizhao.rpc.consumer.common.handler.RpcConsumerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/4/25 15:57
 */
public class RpcConsumerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new RpcEncoder());
        pipeline.addLast(new RpcDecoder());
        pipeline.addLast(new RpcConsumerHandler());
    }
}
