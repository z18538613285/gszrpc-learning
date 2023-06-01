package io.gushizhao.rpc.ratelimiter.base;

import io.gushizhao.rpc.ratelimiter.api.RateLimiterInvoker;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/30 9:48
 */
public abstract class AbstractRateLimiterInvoker implements RateLimiterInvoker {

    /**
     * 在milliSeconds毫秒内最多能够通过的请求个数
     */
    protected int permits;
    /**
     * 毫秒数
     */
    protected int milliSeconds;

    @Override
    public void init(int permits, int milliSeconds) {
        this.permits = permits;
        this.milliSeconds = milliSeconds;
    }
}
