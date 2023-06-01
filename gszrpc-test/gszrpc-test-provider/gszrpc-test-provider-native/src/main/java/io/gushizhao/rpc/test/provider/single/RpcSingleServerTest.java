package io.gushizhao.rpc.test.provider.single;

import io.gushizhao.rpc.provider.RpcSingleServer;
import org.junit.Test;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/4/23 14:20
 */
public class RpcSingleServerTest {

    @Test
    public void startRpcSingleServer() {
        RpcSingleServer singleServer = new RpcSingleServer("127.0.0.1:27880", "127.0.0.1:2181",
                "zookeeper", "zkconsistenthash", "io.gushizhao.rpc.test",
                "asm", 30000, 60000, false, 5000,
                16, 16, "print", 1, "strategy_first",
                false, 2, false, "guava", 1, 5000,
                "fallback" /*direct/fallback/exception*/, true, "counter", 1, 5000,
                "print");
        singleServer.startNettyServer();
    }
}
