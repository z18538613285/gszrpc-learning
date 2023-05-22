package io.gushizhao.rpc.loadbalancer.context;

import io.gushizhao.rpc.protocol.meta.ServiceMeta;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author huzhichao
 * @Description 主要的作用就是存储服务提供者与连接数的对应关系
 * @Date 2023/5/15 10:09
 */
public class ConnectionsContext {
    private static volatile Map<String, Integer> connectionsMap = new ConcurrentHashMap<>();

    public static void add(ServiceMeta serviceMeta) {
        String key = generateKey(serviceMeta);
        Integer count = connectionsMap.get(key);
        if (count == null) {
            count = 0;
        }
        count ++;
        connectionsMap.put(key, count);
    }

    public static Integer getValue(ServiceMeta serviceMeta) {
        String key = generateKey(serviceMeta);
        return connectionsMap.get(key);
    }

    private static String generateKey(ServiceMeta serviceMeta) {
        return serviceMeta.getServiceAddr().concat(":").concat(String.valueOf(serviceMeta.getServicePort()));
    }
}
