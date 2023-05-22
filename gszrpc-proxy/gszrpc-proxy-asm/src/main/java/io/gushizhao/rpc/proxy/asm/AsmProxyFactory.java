package io.gushizhao.rpc.proxy.asm;

import io.gushizhao.rpc.proxy.api.BaseProxyFactory;
import io.gushizhao.rpc.proxy.api.ProxyFactory;
import io.gushizhao.rpc.proxy.asm.proxy.ASMProxy;
import io.gushizhao.rpc.spi.annotation.SPIClass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author huzhichao
 * @Description 主要的作用就是结合 SPI 实现动态扩展 ASM 动态代理功能
 * @Date 2023/5/4 17:56
 */
@SPIClass
public class AsmProxyFactory<T> extends BaseProxyFactory<T> implements ProxyFactory {

    private final Logger logger = LoggerFactory.getLogger(AsmProxyFactory.class);

    @Override
    public <T> T getProxy(Class<T> clazz) {
        logger.info("基于ASM动态代理...");
        try {
            return (T) ASMProxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{clazz}, objectProxy);
        } catch (Exception e) {
            logger.error("asm proxy throws exception:{}", e);
        }
        return null;
    }
}
