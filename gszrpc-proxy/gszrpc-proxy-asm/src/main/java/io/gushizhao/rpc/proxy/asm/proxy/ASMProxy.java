package io.gushizhao.rpc.proxy.asm.proxy;

import io.gushizhao.rpc.proxy.asm.classloader.ASMClassLoader;
import io.gushizhao.rpc.proxy.asm.factory.ASMGenerateProxyFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author huzhichao
 * @Description 主要是作为代理类需要继承的父类，
 * @Date 2023/5/11 19:48
 */
public class ASMProxy {
    // 属性名必须为 h
    protected InvocationHandler h;
    // 代理类名计数器
    private static final AtomicInteger PROXY_CNT = new AtomicInteger(0);

    private static final String PROXY_CLASS_NAME_PKE = "$Proxy";

    public ASMProxy(InvocationHandler var1) {
        this.h = var1;
    }

    public static Object newProxyInstance(ClassLoader loader, Class<?>[] interfaces, InvocationHandler invocationHandler) throws Exception{
        // 生成代理类 Class
        Class<?> proxyClass = generate(interfaces);
        Constructor<?> constructor = proxyClass.getConstructor(InvocationHandler.class);
        return constructor.newInstance(invocationHandler);
    }

    /**
     * 生成代理类 Class
     * @param interfaces 接口的 Class 类型
     * @return 代理类 的 Class对象
     */
    private static Class<?> generate(Class<?>[] interfaces) throws ClassNotFoundException{
        String proxyClassName = PROXY_CLASS_NAME_PKE + PROXY_CNT.getAndIncrement();
        byte[] codes = ASMGenerateProxyFactory.generateClass(interfaces, proxyClassName);
        // 使用自定义类加载器加载字节码
        ASMClassLoader asmClassLoader = new ASMClassLoader();
        asmClassLoader.add(proxyClassName, codes);
        return asmClassLoader.loadClass(proxyClassName);
    }
}
