package io.gushizhao.rpc.test.consumer.spring.annotation.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/22 10:44
 */
@Configuration
@ComponentScan(value = {"io.gushizhao.rpc.*"})
public class SpringAnnotationConsumerConfig {
}
