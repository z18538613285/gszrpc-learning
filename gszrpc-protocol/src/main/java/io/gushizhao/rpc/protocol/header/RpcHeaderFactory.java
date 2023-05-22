package io.gushizhao.rpc.protocol.header;

import io.gushizhao.rpc.common.id.IdFactory;
import io.gushizhao.rpc.constants.RpcConstants;
import io.gushizhao.rpc.protocol.enumeration.RpcType;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/4/23 15:06
 */
public class RpcHeaderFactory {

    public static RpcHeader getRequestHeader(String serialization, int msgType) {
        RpcHeader header = new RpcHeader();
        Long requestId = IdFactory.getId();
        header.setMagic(RpcConstants.MAGIC);
        header.setRequestId(requestId);
        header.setMsgType((byte) msgType);
        header.setStatus((byte) 0x1);
        header.setSerializationType(serialization);
        return header;
    }
}
