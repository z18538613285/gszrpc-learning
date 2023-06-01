package io.gushizhao.rpc.processor.print;

import io.gushizhao.rpc.processor.FlowPostProcessor;
import io.gushizhao.rpc.protocol.header.RpcHeader;
import io.gushizhao.rpc.spi.annotation.SPIClass;
import org.omg.CORBA.PRIVATE_MEMBER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/24 14:42
 */
@SPIClass
public class PrintFlowPostProcessor implements FlowPostProcessor {

    private final Logger logger = LoggerFactory.getLogger(PrintFlowPostProcessor.class);

    @Override
    public void postRpcHeaderProcessor(RpcHeader rpcHeader) {
        logger.info(getRpcHeaderString(rpcHeader));
    }

    private String getRpcHeaderString(RpcHeader rpcHeader) {
        StringBuilder sb = new StringBuilder();
        sb.append("magic: " + rpcHeader.getMagic());
        sb.append(", requestId: " + rpcHeader.getRequestId());
        sb.append(", msgType: " + rpcHeader.getMsgType());
        sb.append(", serializationType: " + rpcHeader.getSerializationType());
        sb.append(", status: " + rpcHeader.getStatus());
        sb.append(", msgLen: " + rpcHeader.getMsgLen());
        return sb.toString();
    }
}
