package io.gushizhao.rpc.loadbalancer.sourceip.hash;

import io.gushizhao.rpc.loadbalancer.api.ServiceLoadBalancer;
import io.gushizhao.rpc.spi.annotation.SPIClass;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/10 10:01
 */
@SPIClass
public class SourceIpHashServiceLoadBalancer<T> implements ServiceLoadBalancer<T> {

    private final static Logger logger = LoggerFactory.getLogger(SourceIpHashServiceLoadBalancer.class);


    @Override
    public T select(List<T> servers, int hashCode, String sourceIp) {
        logger.info("基于源IP地址Hash算法的负载均衡策略...");
        if (servers == null || servers.isEmpty()) {
            return null;
        }
        // 传入的IP地址为空，则默认返回第一个服务实例
        if (StringUtils.isEmpty(sourceIp)) {
            return servers.get(0);
        }
        int resultHashCode = Math.abs(sourceIp.hashCode() + hashCode);
        return servers.get(resultHashCode % servers.size());
    }
}
