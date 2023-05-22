package io.gushizhao.rpc.test.consumer.spring.xml;

import io.gushizhao.rpc.consumer.RpcClient;
import io.gushizhao.rpc.test.api.DemoService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/5 9:16
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:client-spring.xml")
public class SpringXmlConsumerTest {
    private final static Logger logger = LoggerFactory.getLogger(SpringXmlConsumerTest.class);

    @Autowired
    private RpcClient rpcClient;

    @Test
    public void testInterfaceRpc() throws Exception{
        DemoService demoService = rpcClient.create(DemoService.class);
        String result = demoService.hello("gushizhao");
        logger.info("返回的结果数据===>>> " + result);
        rpcClient.shutdown();
        while (true) {
            Thread.sleep(1000);
        }
    }


}
