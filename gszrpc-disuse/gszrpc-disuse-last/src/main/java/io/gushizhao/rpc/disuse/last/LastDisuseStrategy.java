package io.gushizhao.rpc.disuse.last;

import io.gushizhao.rpc.disuse.api.DisuseStrategy;
import io.gushizhao.rpc.disuse.api.connection.ConnectionInfo;
import io.gushizhao.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @Author huzhichao
 * @Description 最晚连接淘汰策略
 * @Date 2023/5/26 14:50
 */
@SPIClass
public class LastDisuseStrategy implements DisuseStrategy {

    private final Logger logger = LoggerFactory.getLogger(LastDisuseStrategy.class);

    @Override
    public ConnectionInfo selectConnection(List<ConnectionInfo> connectionList) {
        logger.info("execute last disuse strategy...");
        if (connectionList.isEmpty()) {
            return null;
        }
        return connectionList.get(connectionList.size() - 1);
    }
}
