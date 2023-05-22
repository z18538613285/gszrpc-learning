package io.gushizhao.rpc.proxy.jdk;

import io.gushizhao.rpc.proxy.api.BaseProxyFactory;
import io.gushizhao.rpc.proxy.api.ProxyFactory;
import io.gushizhao.rpc.proxy.api.consumer.Consumer;
import io.gushizhao.rpc.proxy.api.object.ObjectProxy;
import io.gushizhao.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/4 17:56
 */
@SPIClass
public class JdkProxyFactory<T> extends BaseProxyFactory<T> implements ProxyFactory {

    private final Logger logger = LoggerFactory.getLogger(JdkProxyFactory.class);

    @Override
    public <T> T getProxy(Class<T> clazz) {
        logger.info("基于JDK动态代理...");
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[]{clazz},
                objectProxy);
    }
}
