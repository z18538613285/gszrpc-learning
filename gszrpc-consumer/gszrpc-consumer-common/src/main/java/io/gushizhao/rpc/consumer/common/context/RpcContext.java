package io.gushizhao.rpc.consumer.common.context;

import io.gushizhao.rpc.consumer.common.RpcConsumer;
import io.gushizhao.rpc.proxy.api.future.RPCFuture;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/4 15:26
 */
public class RpcContext {
    private RpcContext () {}

    /**
     * RpcContext 实例
     */
    private static final RpcContext AGENT = new RpcContext();

    /**
     * 存放RPCFuture 的 InheritableThreadLocal
     */
    private static final InheritableThreadLocal<RPCFuture> RPC_FUTURE_INHERITABLE_THREAD_LOCAL = new InheritableThreadLocal<>();

    /**
     * 获取上下文
     *
     * @return RPC 服务的上下文信息
     */
    public static RpcContext getContext() {
        return AGENT;
    }

    /**
     * 将RPCFuture 保存到线程的上下文
     * @param rpcFuture
     */
    public void setRpcFuture(RPCFuture rpcFuture) {
        RPC_FUTURE_INHERITABLE_THREAD_LOCAL.set(rpcFuture);
    }

    /**
     * 获取 RPCFuture
     * @return
     */
    public RPCFuture getRPCFuture() {
        return RPC_FUTURE_INHERITABLE_THREAD_LOCAL.get();
    }

    /**
     * 移除 RPCFuture
     * @return
     */
    public void removeRPCFuture() {
        RPC_FUTURE_INHERITABLE_THREAD_LOCAL.remove();
    }
}
