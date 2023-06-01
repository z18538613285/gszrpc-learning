package io.gushizhao.rpc.disuse.api;


import io.gushizhao.rpc.constants.RpcConstants;
import io.gushizhao.rpc.disuse.api.connection.ConnectionInfo;
import io.gushizhao.rpc.spi.annotation.SPI;

import java.util.List;

@SPI(RpcConstants.RPC_CONNECTION_DISUSE_STRATEGY_DEFAULT)
public interface DisuseStrategy {

    /**
     * 从连接列表中根据规则获取一个连接对象
     */
    ConnectionInfo selectConnection(List<ConnectionInfo> connectionList);

}
