package io.gushizhao.rpc.consumer.common.cache;

import io.netty.channel.Channel;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @Author huzhichao
 * @Description 主要的作用就是在服务消费者端缓存连接服务提供者成功的 Channel
 * @Date 2023/5/15 15:18
 */
public class ConsumerChannelCache {
    private static volatile Set<Channel> channelCache = new CopyOnWriteArraySet<>();

    public static void add(Channel channel) {
        channelCache.add(channel);
    }

    public static void remove(Channel channel) {
        channelCache.remove(channel);
    }

    public static Set<Channel> getChannelCache() {
        return channelCache;
    }
}
