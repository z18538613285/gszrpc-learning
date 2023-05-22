package io.gushizhao.rpc.proxy.javassist;

import io.gushizhao.rpc.proxy.api.BaseProxyFactory;
import io.gushizhao.rpc.proxy.api.ProxyFactory;
import io.gushizhao.rpc.spi.annotation.SPIClass;
import javassist.util.proxy.MethodHandler;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InvocationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/4 17:56
 */
@SPIClass
public class JavassistProxyFactory<T> extends BaseProxyFactory<T> implements ProxyFactory {

    private final Logger logger = LoggerFactory.getLogger(JavassistProxyFactory.class);

    private javassist.util.proxy.ProxyFactory proxyFactory = new javassist.util.proxy.ProxyFactory();

    @Override
    public <T> T getProxy(Class<T> clazz) {
        logger.info("基于javassist动态代理...");
        try {
            proxyFactory.setInterfaces(new Class[]{clazz});
            proxyFactory.setHandler(new MethodHandler() {
                @Override
                public Object invoke(Object o, Method thisMethod, Method proceed, Object[] objects) throws Throwable {
                    return objectProxy.invoke(o, thisMethod, objects);
                }
            });
            // 通过字节码结束动态创建子类实例
            return (T) proxyFactory.createClass().newInstance();
        } catch (Exception e) {
            logger.error("javassist proxy throws exception:{}", e);
        }
        return null;
    }
}
