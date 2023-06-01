package io.gushizhao.rpc.test.provider.spring.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/19 15:14
 */
@SpringBootApplication
@ComponentScan(value = {"io.gushizhao.rpc"})
public class SpringBootProviderStarter {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootProviderStarter.class);
    }
}
