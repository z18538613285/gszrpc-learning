package io.gushizhao.rpc.test.consumer.hello;

import io.gushizhao.rpc.test.api.DemoService;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/29 9:24
 */
public class FallbackDemoServiceImpl implements DemoService {
    @Override
    public String hello(String name) {
        return "fallback hello " + name;
    }
}
