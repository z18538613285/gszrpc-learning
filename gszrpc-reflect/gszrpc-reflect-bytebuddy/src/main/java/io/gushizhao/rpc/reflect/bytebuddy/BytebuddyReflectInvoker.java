package io.gushizhao.rpc.reflect.bytebuddy;

import io.gushizhao.rpc.reflect.api.ReflectInvoker;
import io.gushizhao.rpc.spi.annotation.SPIClass;
import javassist.util.proxy.ProxyFactory;
import net.bytebuddy.ByteBuddy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/12 14:26
 */
@SPIClass
public class BytebuddyReflectInvoker implements ReflectInvoker {
    private final Logger logger = LoggerFactory.getLogger(BytebuddyReflectInvoker.class);
    @Override
    public Object invokeMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Throwable {
        logger.info("use bytebuddy reflect type invoke method...");
        Class<?> childClass = new ByteBuddy().subclass(serviceClass)
                .make()
                .load(BytebuddyReflectInvoker.class.getClassLoader())
                .getLoaded();
        Object instance = childClass.getDeclaredConstructor().newInstance();
        Method method = childClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(instance, parameters);
    }
}
