package io.gushizhao.rpc.proxy.api.async;

import io.gushizhao.rpc.proxy.api.future.RPCFuture;

public interface IAsyncObjectProxy {

    /**
     * 异步代理对象调用方法
     * @param funcName 方法名称
     * @param args 方法参数
     * @return 封装好的 RPCFuture
     */
    RPCFuture call(String funcName, Object... args);
}
