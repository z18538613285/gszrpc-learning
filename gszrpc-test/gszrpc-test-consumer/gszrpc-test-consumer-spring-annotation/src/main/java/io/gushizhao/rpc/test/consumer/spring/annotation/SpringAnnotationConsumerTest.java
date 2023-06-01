package io.gushizhao.rpc.test.consumer.spring.annotation;

import io.gushizhao.rpc.consumer.RpcClient;
import io.gushizhao.rpc.test.api.DemoService;
import io.gushizhao.rpc.test.consumer.spring.annotation.config.SpringAnnotationConsumerConfig;
import io.gushizhao.rpc.test.consumer.spring.annotation.service.ConsumerDemoService;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/5 9:16
 */
public class SpringAnnotationConsumerTest {
    private final static Logger logger = LoggerFactory.getLogger(SpringAnnotationConsumerTest.class);


    @Test
    public void testInterfaceRpc() throws Exception{
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringAnnotationConsumerConfig.class);
        ConsumerDemoService consumerDemoService = context.getBean(ConsumerDemoService.class);
        String result = consumerDemoService.hello("gushizhao");
        logger.info("返回的结果数据===>>> " + result);
    }

}
