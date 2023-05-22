package io.gushizhao.rpc.test.provider.spring.xml;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/19 15:14
 */
public class SpringXmlProviderStarter {
    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("server-spring.xml");
    }
}
