package io.gushizhao.rpc.disuse.fifo;

import io.gushizhao.rpc.disuse.api.DisuseStrategy;
import io.gushizhao.rpc.disuse.api.connection.ConnectionInfo;
import io.gushizhao.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @Author huzhichao
 * @Description 先进先出连接淘汰策略
 * @Date 2023/5/26 14:50
 */
@SPIClass
public class FifoDisuseStrategy implements DisuseStrategy {

    private final Logger logger = LoggerFactory.getLogger(FifoDisuseStrategy.class);

    private final Comparator<ConnectionInfo> connectionTimeComparator = (o1, o2) -> {
      return o1.getConnectionTime() - o2.getConnectionTime() > 0 ? 1 : -1;
    };

    @Override
    public ConnectionInfo selectConnection(List<ConnectionInfo> connectionList) {
        logger.info("execute fifo disuse strategy...");
        if (connectionList.isEmpty()) {
            return null;
        }
        Collections.sort(connectionList, connectionTimeComparator);
        return connectionList.get(0);
    }
}
