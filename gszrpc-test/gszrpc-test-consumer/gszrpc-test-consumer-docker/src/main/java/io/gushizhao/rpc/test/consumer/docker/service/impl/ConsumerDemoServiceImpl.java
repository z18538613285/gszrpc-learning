package io.gushizhao.rpc.test.consumer.docker.service.impl;

import io.gushizhao.rpc.annotation.RpcReference;
import io.gushizhao.rpc.test.api.DemoService;
import io.gushizhao.rpc.test.consumer.docker.service.ConsumerDemoService;
import org.springframework.stereotype.Service;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/22 10:22
 */
@Service
public class ConsumerDemoServiceImpl implements ConsumerDemoService {

    @RpcReference(registryType = "zookeeper", registryAddress = "127.0.0.1:2181", loadBalanceType = "zkconsistenthash", version = "1.0.0", group = "gushizhao", serializationType = "protostuff", proxy = "cglib", timeout = 30000, async = false, onwway = false)
    private DemoService demoService;

    @Override
    public String hello(String name) {
        return demoService.hello(name);
    }
}
