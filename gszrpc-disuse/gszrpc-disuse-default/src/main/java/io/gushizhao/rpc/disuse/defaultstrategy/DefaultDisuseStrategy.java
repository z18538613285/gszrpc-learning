package io.gushizhao.rpc.disuse.defaultstrategy;

import io.gushizhao.rpc.disuse.api.DisuseStrategy;
import io.gushizhao.rpc.disuse.api.connection.ConnectionInfo;
import io.gushizhao.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @Author huzhichao
 * @Description 默认淘汰策略
 * @Date 2023/5/26 14:50
 */
@SPIClass
public class DefaultDisuseStrategy implements DisuseStrategy {

    private final Logger logger = LoggerFactory.getLogger(DefaultDisuseStrategy.class);

    @Override
    public ConnectionInfo selectConnection(List<ConnectionInfo> connectionList) {
        logger.info("execute default disuse strategy...");
        return connectionList.get(0);
    }
}
