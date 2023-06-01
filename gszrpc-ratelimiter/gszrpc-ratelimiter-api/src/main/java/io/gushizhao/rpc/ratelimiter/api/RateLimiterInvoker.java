package io.gushizhao.rpc.ratelimiter.api;

import io.gushizhao.rpc.constants.RpcConstants;
import io.gushizhao.rpc.spi.annotation.SPI;

@SPI(RpcConstants.DEFAULT_RATELIMITER_INVOKER)
public interface RateLimiterInvoker {


    /**
     * 限流方法
     * 尝试获取资源，成功返回true，失败false
     */
    boolean tryAcquire();

    /**
     * 释放资源
     */
    void release();

    /**
     * 在milliSeconds毫秒内最多允许通过permits个请求
     * @param permits 在milliSeconds毫秒内最多能够通过的请求个数
     * @param milliSeconds 毫秒数
     */
    default void init(int permits, int milliSeconds){}
}
