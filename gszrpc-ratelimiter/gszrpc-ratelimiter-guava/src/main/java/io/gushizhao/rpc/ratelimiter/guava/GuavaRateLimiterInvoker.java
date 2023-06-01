package io.gushizhao.rpc.ratelimiter.guava;

import com.google.common.util.concurrent.RateLimiter;
import io.gushizhao.rpc.ratelimiter.base.AbstractRateLimiterInvoker;
import io.gushizhao.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/30 9:49
 */
@SPIClass
public class GuavaRateLimiterInvoker extends AbstractRateLimiterInvoker {
    private final Logger logger = LoggerFactory.getLogger(GuavaRateLimiterInvoker.class);
    private RateLimiter rateLimiter;

    @Override
    public void init(int permits, int milliSeconds) {
        super.init(permits, milliSeconds);
        // 转换成 每秒钟最多允许的个数
        double permitsPerSecond = ((double) permits) / milliSeconds * 1000;
        this.rateLimiter = RateLimiter.create(permitsPerSecond);
    }

    @Override
    public boolean tryAcquire() {
        logger.info("execute gauva rate limiter...");
        return this.rateLimiter.tryAcquire();
    }

    @Override
    public void release() {
        //TODO ignore
    }
}
