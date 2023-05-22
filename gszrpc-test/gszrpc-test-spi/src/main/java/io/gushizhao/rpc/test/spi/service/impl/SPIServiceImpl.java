package io.gushizhao.rpc.test.spi.service.impl;

import io.gushizhao.rpc.spi.annotation.SPIClass;
import io.gushizhao.rpc.test.spi.service.SPIService;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/11 14:58
 */
@SPIClass
public class SPIServiceImpl implements SPIService {
    @Override
    public String hello(String name) {
        return "hello " + name;
    }
}
