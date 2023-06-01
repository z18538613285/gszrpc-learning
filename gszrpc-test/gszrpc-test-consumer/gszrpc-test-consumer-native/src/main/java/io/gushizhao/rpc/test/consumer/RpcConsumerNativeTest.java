package io.gushizhao.rpc.test.consumer;

import io.gushizhao.rpc.consumer.RpcClient;
import io.gushizhao.rpc.proxy.api.async.IAsyncObjectProxy;
import io.gushizhao.rpc.proxy.api.future.RPCFuture;
import io.gushizhao.rpc.test.api.DemoService;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/5 9:16
 */
public class RpcConsumerNativeTest {
    private final static Logger logger = LoggerFactory.getLogger(RpcConsumerNativeTest.class);
    private RpcClient rpcClient;

    @Before
    public void initRpcClient() {
        rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "enhanced_leastconnections",
                "asm", "1.0.0", "gushizhao", 3000, "protostuff", false,
                false, 30000, 60000, 1000, 3, false,
                10000, true, "127.0.0.1:27880,127.0.0.1:27880,127.0.0.1:27880",
                true, 16, 16, "print", false, 2,
                "asm", "io.gushizhao.rpc.test.consumer.hello.FallbackDemoServiceImpl",
                false, "guava", 5, 5000,"fallback",
                true, "percent", 10, 10000, "print");
    }

    @Test
    public void testInterfaceRpc() throws Exception {
        DemoService demoService = rpcClient.create(DemoService.class);
        //Thread.sleep(5000);
        for (int i = 0; i < 5; i++) {
            String result = demoService.hello("gsz");
            logger.info("返回的结果数据===>>> " + result);
        }
        while (true) {
            Thread.sleep(1000);
        }
    }

    public static void main(String[] args) throws Exception {
        RpcClient rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "random",
                "jdk", "1.0.0", "gushizhao", 3000, "jdk", false,
                false, 30000, 60000, 1000, 3, false,
                10000, false, "127.0.0.1:27880", true, 16,
                16, "print", false, 2, "jdk",
                "io.gushizhao.rpc.test.consumer.hello.FallbackDemoServiceImpl", false,
                "counter", 100, 1000, "fallback",
                true, "percent", 1, 5000,"print");

        DemoService demoService = rpcClient.create(DemoService.class);
        String result = demoService.hello("gushizhao");
        logger.info("返回的结果数据===>>> " + result);
        rpcClient.shutdown();

/*        RpcClient rpcClient = new RpcClient("1.0.0", "gushizhao", 3000, "jdk", false, false);
        IAsyncObjectProxy demoService = rpcClient.createAsync(DemoService.class);
        RPCFuture future = demoService.call("hello","gushizhao");
        logger.info("返回的结果数据===>>> " + future.get());
        rpcClient.shutdown();*/
    }

}
