package io.gushizhao.rpc.spi.annotation;

import java.lang.annotation.*;

/**
 * 主要标注到加入 SPI 机制的接口的实现类上
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SPIClass {
}
