package io.gushizhao.rpc.test.scanner.consumer.service.impl;

import io.gushizhao.rpc.annotation.RpcReference;
import io.gushizhao.rpc.test.scanner.consumer.service.ConsumerBusinessService;
import io.gushizhao.rpc.test.scanner.service.DemoService;

/**
 * @Author huzhichao
 * @Description 服务消费者业务逻辑实现类
 * @Date 2023/4/21 17:57
 */
public class ConsumerBusinessServiceImpl implements ConsumerBusinessService {

    @RpcReference(registryType = "zookeeper", registryAddress = "127.0.0.1:2181", version = "1.0.0", group = "gushizhao")
    private DemoService demoService;
}
