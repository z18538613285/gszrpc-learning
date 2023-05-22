package io.gushizhao.rpc.proxy.asm.classloader;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author huzhichao
 * @Description 主要的作用就是加载基于ASM 动态生成的代理类
 * @Date 2023/5/11 19:44
 */
public class ASMClassLoader extends ClassLoader{
    private final Map<String, byte[]> classMap = new HashMap<>();

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (classMap.containsKey(name)) {
            byte[] bytes = classMap.get(name);
            classMap.remove(name);
            return defineClass(name, bytes, 0, bytes.length);
        }
        return super.findClass(name);
    }
    public void add(String name, byte[] bytes) {
        classMap.put(name, bytes);
    }
}
