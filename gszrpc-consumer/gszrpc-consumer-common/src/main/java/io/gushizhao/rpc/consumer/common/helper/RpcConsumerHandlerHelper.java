package io.gushizhao.rpc.consumer.common.helper;

import io.gushizhao.rpc.consumer.common.handler.RpcConsumerHandler;
import io.gushizhao.rpc.protocol.meta.ServiceMeta;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author huzhichao
 * @Description 主要用于缓存服务消费者处理器 RpcConsumerHandler 类的实例
 * @Date 2023/5/8 19:07
 */
public class RpcConsumerHandlerHelper {

    private static Map<String, RpcConsumerHandler> rpcConsumerHandlerMap;

    static {
        rpcConsumerHandlerMap = new ConcurrentHashMap<>();
    }

    private static String getKey(ServiceMeta key) {
        return key.getServiceAddr().concat("_").concat(String.valueOf(key.getServicePort()));
    }

    public static void put(ServiceMeta key, RpcConsumerHandler value) {
        rpcConsumerHandlerMap.put(getKey(key), value);
    }

    public static RpcConsumerHandler get(ServiceMeta key) {
        return rpcConsumerHandlerMap.get(getKey(key));
    }

    public static void closeRpcClientHandler() {
        Collection<RpcConsumerHandler> rpcConsumerHandlers = rpcConsumerHandlerMap.values();
        if (rpcConsumerHandlers != null) {
            rpcConsumerHandlers.stream().forEach((rpcConsumerHandler) -> {
                rpcConsumerHandler.close();
            });
        }
        rpcConsumerHandlerMap.clear();
    }
}
