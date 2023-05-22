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
        rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "enhanced_leastconnections","asm", "1.0.0", "gushizhao", 3000, "protostuff", false, false, 30000, 60000, 1000, 3);
    }

    @Test
    public void testInterfaceRpc() throws Exception{
        DemoService demoService = rpcClient.create(DemoService.class);
        String result = demoService.hello("gushizhao");
        logger.info("返回的结果数据===>>> " + result);
        Thread.sleep(120000);
        rpcClient.shutdown();
    }

    public static void main(String[] args) throws Exception{
        RpcClient rpcClient = new RpcClient("127.0.0.1:2181", "zookeeper", "random", "jdk","1.0.0", "gushizhao", 3000, "jdk", false, false, 30000, 60000, 1000, 3);
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
