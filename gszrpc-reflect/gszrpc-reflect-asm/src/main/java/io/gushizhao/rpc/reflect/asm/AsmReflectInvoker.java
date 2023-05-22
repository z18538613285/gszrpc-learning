package io.gushizhao.rpc.reflect.asm;

import io.gushizhao.rpc.reflect.api.ReflectInvoker;
import io.gushizhao.rpc.reflect.asm.proxy.ReflectProxy;
import io.gushizhao.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/12 14:26
 */
@SPIClass
public class AsmReflectInvoker implements ReflectInvoker {
    private final Logger logger = LoggerFactory.getLogger(AsmReflectInvoker.class);
    @Override
    public Object invokeMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Throwable {
        logger.info("use asm reflect type invoke method...");
        Constructor<?> constructor = serviceClass.getConstructor(new Class[]{});
        Object[] constructorParam = new Object[]{};
        Object instance = ReflectProxy.newProxyInstance(AsmReflectInvoker.class.getClassLoader(), getInvocationHandler(serviceBean), serviceClass, constructor, constructorParam);

        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(instance, parameters);
    }

    private InvocationHandler getInvocationHandler(Object serviceBean) {
        return ((proxy, method, args) -> {
           logger.info("use proxy invoke method...");
           method.setAccessible(true);
            Object result = method.invoke(serviceBean, args);
            return result;
        });
    }

}
