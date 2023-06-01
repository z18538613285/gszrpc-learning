package io.gushizhao.rpc.processor;

import io.gushizhao.rpc.constants.RpcConstants;
import io.gushizhao.rpc.protocol.header.RpcHeader;
import io.gushizhao.rpc.spi.annotation.SPI;

@SPI(RpcConstants.FLOW_POST_PROCESSOR_PRINT)
public interface FlowPostProcessor {

    /**
     * 流控分析后置处理器方法
     * @param rpcHeader
     */
    void postRpcHeaderProcessor(RpcHeader rpcHeader);
}
