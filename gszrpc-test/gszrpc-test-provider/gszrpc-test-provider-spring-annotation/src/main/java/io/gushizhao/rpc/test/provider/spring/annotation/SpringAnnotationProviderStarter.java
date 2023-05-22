package io.gushizhao.rpc.test.provider.spring.annotation;

import io.gushizhao.rpc.test.provider.spring.annotation.config.SpringAnnotationProviderConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/19 15:14
 */
public class SpringAnnotationProviderStarter {
    public static void main(String[] args) {
        new AnnotationConfigApplicationContext(SpringAnnotationProviderConfig.class);
    }
}
