package io.gushizhao.rpc.provider.common.server.api;

/**
 * @Author huzhichao
 * @Description 启动 gszrpc 框架服务提供者的核心接口
 * @Date 2023/4/23 11:39
 */
public interface Server {

    /**
     * 启动Netty 服务
     */
    void startNettyServer();
}
