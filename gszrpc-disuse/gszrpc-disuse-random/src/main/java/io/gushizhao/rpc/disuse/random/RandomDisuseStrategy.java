package io.gushizhao.rpc.disuse.random;

import io.gushizhao.rpc.disuse.api.DisuseStrategy;
import io.gushizhao.rpc.disuse.api.connection.ConnectionInfo;
import io.gushizhao.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * @Author huzhichao
 * @Description 随机连接淘汰策略
 * @Date 2023/5/26 14:50
 */
@SPIClass
public class RandomDisuseStrategy implements DisuseStrategy {

    private final Logger logger = LoggerFactory.getLogger(RandomDisuseStrategy.class);

    @Override
    public ConnectionInfo selectConnection(List<ConnectionInfo> connectionList) {
        logger.info("execute random disuse strategy...");
        if (connectionList.isEmpty()) {
            return null;
        }
        return connectionList.get(new Random().nextInt(connectionList.size()));
    }
}
