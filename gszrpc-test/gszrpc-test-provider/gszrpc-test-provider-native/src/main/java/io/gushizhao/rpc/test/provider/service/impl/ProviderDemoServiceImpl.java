package io.gushizhao.rpc.test.provider.service.impl;

import io.gushizhao.rpc.annotation.RpcService;
import io.gushizhao.rpc.common.exception.RpcException;
import io.gushizhao.rpc.test.api.DemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/4/23 14:17
 */
@RpcService(interfaceClass = DemoService.class,
interfaceClassName = "io.gushizhao.rpc.test.api.DemoService",
version = "1.0.0",
group = "gushizhao")
public class ProviderDemoServiceImpl implements DemoService {
    private static final Logger logger = LoggerFactory.getLogger(ProviderDemoServiceImpl.class);

    @Override
    public String hello(String name) {
        logger.info("调用hello方法传入的参数为===>>>{}", name);
        if ("gushizhao".equals(name)) {
            throw new RpcException("rpc provider throws exception");
        }
        return "hello " + name;
    }
}
