package io.gushizhao.rpc.proxy.bytebuddy;

import io.gushizhao.rpc.proxy.api.BaseProxyFactory;
import io.gushizhao.rpc.proxy.api.ProxyFactory;
import io.gushizhao.rpc.spi.annotation.SPIClass;
import javassist.util.proxy.MethodHandler;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/4 17:56
 */
@SPIClass
public class BytebuddyProxyFactory<T> extends BaseProxyFactory<T> implements ProxyFactory {

    private final Logger logger = LoggerFactory.getLogger(BytebuddyProxyFactory.class);

    private javassist.util.proxy.ProxyFactory proxyFactory = new javassist.util.proxy.ProxyFactory();

    @Override
    public <T> T getProxy(Class<T> clazz) {
        logger.info("基于ByteBuddy动态代理...");
        try {
            return (T) new ByteBuddy().subclass(Object.class)
                    .implement(clazz)
                    .intercept(InvocationHandlerAdapter.of(objectProxy))
                    .make()
                    .load(BytebuddyProxyFactory.class.getClassLoader())
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (Exception e) {
            logger.error("bytebuddy proxy throws exception:{}", e);
        }
        return null;
    }
}
