package io.gushizhao.rpc.disuse.first;

import io.gushizhao.rpc.disuse.api.DisuseStrategy;
import io.gushizhao.rpc.disuse.api.connection.ConnectionInfo;
import io.gushizhao.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @Author huzhichao
 * @Description 最早连接淘汰策略
 * @Date 2023/5/26 14:50
 */
@SPIClass
public class FirstDisuseStrategy implements DisuseStrategy {

    private final Logger logger = LoggerFactory.getLogger(FirstDisuseStrategy.class);

    @Override
    public ConnectionInfo selectConnection(List<ConnectionInfo> connectionList) {
        logger.info("execute first disuse strategy...");
        return connectionList.get(0);
    }
}
