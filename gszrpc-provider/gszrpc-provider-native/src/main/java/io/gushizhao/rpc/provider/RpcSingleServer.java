package io.gushizhao.rpc.provider;

import io.gushizhao.rpc.provider.common.server.base.BaseServer;
import io.gushizhao.rpc.common.scanner.server.RpcServiceScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/4/23 14:03
 */
public class RpcSingleServer extends BaseServer {
    private final Logger logger = LoggerFactory.getLogger(RpcSingleServer.class);

    public RpcSingleServer(String serverAdress, String scanPackage, String reflectType) {
        // 调用 父类构造方法
        super(serverAdress, reflectType);
        try {
            this.handlerMap = RpcServiceScanner.doScannerWithRpcServiceAnnotationFilterAndRegistryService(scanPackage);
        } catch (Exception e) {
            logger.error("RPC Server init error", e);
        }
    }

}
