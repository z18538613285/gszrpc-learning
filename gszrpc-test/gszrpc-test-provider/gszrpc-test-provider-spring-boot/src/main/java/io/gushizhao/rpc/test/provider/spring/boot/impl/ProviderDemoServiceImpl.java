package io.gushizhao.rpc.test.provider.spring.boot.impl;

import io.gushizhao.rpc.annotation.RpcService;
import io.gushizhao.rpc.test.api.DemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/4/23 14:17
 */
@RpcService(interfaceClass = DemoService.class,
interfaceClassName = "io.gushizhao.rpc.test.api.DemoService",
version = "1.0.0",
group = "gushizhao",
weight = 2)
@Service
public class ProviderDemoServiceImpl implements DemoService {
    private static final Logger logger = LoggerFactory.getLogger(ProviderDemoServiceImpl.class);

    @Override
    public String hello(String name) {
        logger.info("调用hello方法传入的参数为===>>>{}", name);
        return "hello " + name;
    }
}
