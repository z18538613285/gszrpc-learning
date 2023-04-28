package io.gushizhao.rpc.common.helper;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/4/23 17:42
 */
public class RpcServiceHelper {

    public static String buildServiceKey(String serviceName, String serviceVersion, String group) {
        return String.join("#", serviceName, serviceVersion, group);
    }
}
