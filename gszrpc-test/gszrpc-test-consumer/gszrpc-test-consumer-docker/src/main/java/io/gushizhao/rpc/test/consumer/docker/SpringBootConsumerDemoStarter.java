package io.gushizhao.rpc.test.consumer.docker;

import io.gushizhao.rpc.test.consumer.docker.service.ConsumerDemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/5 9:16
 */
@SpringBootApplication
@ComponentScan(basePackages = {"io.gushizhao.rpc"})
public class SpringBootConsumerDemoStarter {
    private final static Logger logger = LoggerFactory.getLogger(SpringBootConsumerDemoStarter.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SpringBootConsumerDemoStarter.class);
        ConsumerDemoService consumerDemoService = context.getBean(ConsumerDemoService.class);
        String result = consumerDemoService.hello("gushizhao");
        logger.info("返回的结果数据===>>> " + result);
    }

}
