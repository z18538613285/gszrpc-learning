package io.gushizhao.rpc.test.consumer.codec.init;

import io.gushizhao.rpc.codec.RpcDecoder;
import io.gushizhao.rpc.codec.RpcEncoder;
import io.gushizhao.rpc.test.consumer.codec.handler.RpcTestConsumerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/4/23 17:21
 */
public class RpcTestConsumerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline cp = socketChannel.pipeline();
        cp.addLast(new RpcEncoder());
        cp.addLast(new RpcDecoder());
        cp.addLast(new RpcTestConsumerHandler());
    }
}
