package io.gushizhao.rpc.reflect.javassist;

import io.gushizhao.rpc.reflect.api.ReflectInvoker;
import io.gushizhao.rpc.spi.annotation.SPIClass;
import javassist.util.proxy.ProxyFactory;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/12 14:26
 */
@SPIClass
public class JavassistReflectInvoker implements ReflectInvoker {
    private final Logger logger = LoggerFactory.getLogger(JavassistReflectInvoker.class);
    @Override
    public Object invokeMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Throwable {
        logger.info("use javassist reflect type invoke method...");
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setSuperclass(serviceClass);
        Class<?> childClass = proxyFactory.createClass();
        Method method = childClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(childClass.newInstance(), parameters);
    }
}
