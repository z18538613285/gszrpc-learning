package io.gushizhao.rpc.reflect.cglib;

import io.gushizhao.rpc.reflect.api.ReflectInvoker;
import io.gushizhao.rpc.spi.annotation.SPIClass;
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
public class CglibReflectInvoker implements ReflectInvoker {
    private final Logger logger = LoggerFactory.getLogger(CglibReflectInvoker.class);
    @Override
    public Object invokeMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Throwable {
        logger.info("use cglib reflect type invoke method...");
        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastClassMethod = serviceFastClass.getMethod(methodName, parameterTypes);
        return serviceFastClassMethod.invoke(serviceBean, parameters);
    }
}
