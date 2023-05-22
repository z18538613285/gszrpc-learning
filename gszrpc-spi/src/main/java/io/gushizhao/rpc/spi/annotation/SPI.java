package io.gushizhao.rpc.spi.annotation;

import java.lang.annotation.*;

/**
 * 主要标注到加入 SPI 机制的接口上
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SPI {
    String value() default "";
}
