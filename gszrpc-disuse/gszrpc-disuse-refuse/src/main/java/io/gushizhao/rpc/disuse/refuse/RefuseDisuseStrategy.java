package io.gushizhao.rpc.disuse.refuse;

import io.gushizhao.rpc.common.exception.RefuseException;
import io.gushizhao.rpc.disuse.api.DisuseStrategy;
import io.gushizhao.rpc.disuse.api.connection.ConnectionInfo;
import io.gushizhao.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

/**
 * @Author huzhichao
 * @Description 拒绝连接淘汰策略
 * @Date 2023/5/26 14:50
 */
@SPIClass
public class RefuseDisuseStrategy implements DisuseStrategy {

    private final Logger logger = LoggerFactory.getLogger(RefuseDisuseStrategy.class);

    @Override
    public ConnectionInfo selectConnection(List<ConnectionInfo> connectionList) {
        logger.info("execute refuse disuse strategy...");
        throw new RefuseException("refuse new connection...");
    }
}
