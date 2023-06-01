package io.gushizhao.rpc.disuse.lru;

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
 * @Description 最近最少使用连接策略
 * @Date 2023/5/26 14:50
 */
@SPIClass
public class LruDisuseStrategy implements DisuseStrategy {

    private final Logger logger = LoggerFactory.getLogger(LruDisuseStrategy.class);

    private final Comparator<ConnectionInfo> lastUseTimeComparator = (o1, o2) -> {
      return o1.getLastUseTime() - o2.getLastUseTime() > 0 ? 1 : -1;
    };

    @Override
    public ConnectionInfo selectConnection(List<ConnectionInfo> connectionList) {
        logger.info("execute lru disuse strategy...");
        if (connectionList.isEmpty()) {
            return null;
        }
        Collections.sort(connectionList, lastUseTimeComparator);
        return connectionList.get(0);
    }
}
