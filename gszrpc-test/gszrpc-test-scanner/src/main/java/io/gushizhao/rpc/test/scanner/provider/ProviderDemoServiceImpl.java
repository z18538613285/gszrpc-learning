package io.gushizhao.rpc.test.scanner.provider;

import io.gushizhao.rpc.annotation.RpcService;
import io.gushizhao.rpc.test.scanner.service.DemoService;

/**
 * @Author huzhichao
 * @Description DemoService 实现类
 * @Date 2023/4/21 17:53
 */
@RpcService(interfaceClass = DemoService.class, interfaceClassName = "io.gushizhao.rpc.test.scanner.service.DemoService",
version = "1.0.0", group = "gushizhao")
public class ProviderDemoServiceImpl implements DemoService {
}
