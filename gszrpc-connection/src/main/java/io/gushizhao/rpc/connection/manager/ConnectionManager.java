package io.gushizhao.rpc.connection.manager;

import io.gushizhao.rpc.common.exception.RefuseException;
import io.gushizhao.rpc.constants.RpcConstants;
import io.gushizhao.rpc.disuse.api.DisuseStrategy;
import io.gushizhao.rpc.disuse.api.connection.ConnectionInfo;
import io.gushizhao.rpc.spi.loader.ExtensionLoader;
import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/26 14:53
 */
public class ConnectionManager {

    private volatile Map<String, ConnectionInfo> connectionMap = new ConcurrentHashMap<>();

    private final DisuseStrategy disuseStrategy;
    private final int maxConnections;
    private static volatile ConnectionManager instance;

    private ConnectionManager(int maxConnections, String disuseStrategyType){
        this.maxConnections = maxConnections <= 0 ? Integer.MAX_VALUE : maxConnections;
        disuseStrategyType = StringUtils.isEmpty(disuseStrategyType) ? RpcConstants.RPC_CONNECTION_DISUSE_STRATEGY_DEFAULT : disuseStrategyType;
        this.disuseStrategy = ExtensionLoader.getExtension(DisuseStrategy.class, disuseStrategyType);
    }

    /**
     * 单例模式
     */
    public static ConnectionManager getInstance(int maxConnections, String disuseStrategyType){
        if (instance == null){
            synchronized (ConnectionManager.class){
                if (instance == null){
                    instance = new ConnectionManager(maxConnections, disuseStrategyType);
                }
            }
        }
        return instance;
    }

    /**
     * 添加连接
     */
    public void add(Channel channel){
        ConnectionInfo info = new ConnectionInfo(channel);
        if (this.checkConnectionList(info)){
            connectionMap.put(getKey(channel), info);
        }
    }

    /**
     * 移除连接
     */
    public void remove(Channel channel){
        connectionMap.remove(getKey(channel));
    }

    /**
     * 更新连接信息
     */
    public void update(Channel channel){
        ConnectionInfo info = connectionMap.get(getKey(channel));
        info.setLastUseTime(System.currentTimeMillis());
        info.incrementUseCount();
        connectionMap.put(getKey(channel), info);
    }



    /**
     * 检测连接列表
     */
    private boolean checkConnectionList(ConnectionInfo info) {
        List<ConnectionInfo> connectionList = new ArrayList<>(connectionMap.values());
        if (connectionList.size() >= maxConnections){
            try{
                ConnectionInfo cacheConnectionInfo = disuseStrategy.selectConnection(connectionList);
                if (cacheConnectionInfo != null){
                    cacheConnectionInfo.getChannel().close();
                    connectionMap.remove(getKey(cacheConnectionInfo.getChannel()));
                }
            }catch (RefuseException e){
                info.getChannel().close();
                return false;
            }
        }
        return true;
    }

    private String getKey(Channel channel){
        return channel.id().asLongText();
    }
}
