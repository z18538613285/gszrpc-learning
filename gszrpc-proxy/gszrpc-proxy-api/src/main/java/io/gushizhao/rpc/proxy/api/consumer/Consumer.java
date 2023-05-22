package io.gushizhao.rpc.proxy.api.consumer;

import io.gushizhao.rpc.protocol.RpcProtocol;
import io.gushizhao.rpc.protocol.request.RpcRequest;
import io.gushizhao.rpc.proxy.api.future.RPCFuture;
import io.gushizhao.rpc.registry.api.RegistryService;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/4 17:10
 */
public interface Consumer {

    /**
     * 消费者发送 request 请求
     * @param protocol
     * @return
     * @throws Exception
     */
    RPCFuture sendRequest(RpcProtocol<RpcRequest> protocol, RegistryService registryService) throws Exception;

}
