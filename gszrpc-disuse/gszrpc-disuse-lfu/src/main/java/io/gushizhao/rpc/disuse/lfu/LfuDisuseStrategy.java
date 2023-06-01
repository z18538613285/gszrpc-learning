package io.gushizhao.rpc.disuse.lfu;

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
 * @Description 使用次数最少连接策略
 * @Date 2023/5/26 14:50
 */
@SPIClass
public class LfuDisuseStrategy implements DisuseStrategy {

    private final Logger logger = LoggerFactory.getLogger(LfuDisuseStrategy.class);

    private final Comparator<ConnectionInfo> useCountComparator = (o1, o2) -> {
      return o1.getUseCount() - o2.getUseCount() > 0 ? 1 : -1;
    };

    @Override
    public ConnectionInfo selectConnection(List<ConnectionInfo> connectionList) {
        logger.info("execute lfu disuse strategy...");
        if (connectionList.isEmpty()) {
            return null;
        }
        Collections.sort(connectionList, useCountComparator);
        return connectionList.get(0);
    }
}
