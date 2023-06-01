package io.gushizhao.rpc.ratelimiter.counter;

import io.gushizhao.rpc.ratelimiter.base.AbstractRateLimiterInvoker;
import io.gushizhao.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/30 9:49
 */
@SPIClass
public class CounterRateLimiterInvoker extends AbstractRateLimiterInvoker {
    private final Logger logger = LoggerFactory.getLogger(CounterRateLimiterInvoker.class);
    private final AtomicInteger currentCounter = new AtomicInteger(0);
    private volatile long lastTimeStamp = System.currentTimeMillis();

    @Override
    public boolean tryAcquire() {
        logger.info("execute counter rate limiter...");
        //获取当前时间
        long currentTimeStamp = System.currentTimeMillis();
        //超过一个执行周期
        if (currentTimeStamp - lastTimeStamp >= milliSeconds){
            lastTimeStamp = currentTimeStamp;
            currentCounter.set(0);
            return true;
        }
        //当前请求数小于配置的数量
        if (currentCounter.incrementAndGet() <= permits){
            return true;
        }
        return false;
    }

    /**
     * fix05
     * 问题
     * 在设计上，基于计数器的限流策略，要实现的逻辑就是在一段时间内，最多允许通过多少个请求，如果请求的数量在这段时间内，达到了上限，就需要触发限流的操作。
     * 但是，在实际实现过程中，会出现在这段时间内，达到请求数量的上限时，没有触发限流的问题。
     *
     * 解决
     * 结合前面的代码分析得出结论：当请求获取到限流资源，执行完业务逻辑后，就会在finally代码块中调用release()方法释放资源，
     * 则后续的请求就会再次获取到限流资源，继续执行后续的业务逻辑，达不到限流的效果。
     *去除threadLocal相关的操作
     */
    @Override
    public void release() {
        //TODO ignore
        //if (threadLocal.get()){
        //    try {
        //        currentCounter.decrementAndGet();
        //    }finally {
        //        threadLocal.remove();
        //    }
        //}
    }
}
