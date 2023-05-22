package io.gushizhao.rpc.spi.factory;

import io.gushizhao.rpc.spi.annotation.SPI;
import io.gushizhao.rpc.spi.annotation.SPIClass;
import io.gushizhao.rpc.spi.loader.ExtensionLoader;

import java.util.Optional;

/**
 * @Author huzhichao
 * @Description 表示基于SPI 实现的扩展类加载器工厂类
 * @Date 2023/5/10 10:40
 */
@SPIClass
public class SpiExtensionFactory implements ExtensionFactory{
    @Override
    public <T> T getExtension(String key, Class<T> clazz) {
       return Optional.ofNullable(clazz)
                .filter(cls -> cls.isAnnotationPresent(SPI.class))
                .map(ExtensionLoader::getExtensionLoader)
                .map(ExtensionLoader::getDefaultSpiClassInstance)
                .orElse(null);
    }
}
