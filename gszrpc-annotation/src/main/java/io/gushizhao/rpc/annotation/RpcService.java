package io.gushizhao.rpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author gushizhao
 * @version 1.0.0
 * @description gszrpc 服务提供者注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RpcService {

    /**
     * 接口的 Class
     * @return
     */
    Class<?> interfaceClass() default void.class;

    /**
     * 接口的 ClassName
     * @return
     */
    String interfaceClassName() default "";

    /**
     * 版本号
     * @return
     */
    String version() default "1.0.0";

    /**
     * 服务分组，默认为空
     * @return
     */
    String group() default "";
}
