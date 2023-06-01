package io.gushizhao.rpc.ratelimiter.semaphore;

import io.gushizhao.rpc.ratelimiter.base.AbstractRateLimiterInvoker;
import io.gushizhao.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/30 9:49
 */
@SPIClass
public class SemaphoreRateLimiterInvoker extends AbstractRateLimiterInvoker {
    private final Logger logger = LoggerFactory.getLogger(SemaphoreRateLimiterInvoker.class);
    private Semaphore semaphore;
    private final AtomicInteger currentCounter = new AtomicInteger(0);
    private volatile long lastTimeStamp = System.currentTimeMillis();

    @Override
    public void init(int permits, int milliSeconds) {
        super.init(permits, milliSeconds);
        this.semaphore = new Semaphore(permits);
    }

    @Override
    public boolean tryAcquire() {
        logger.info("execute semaphore rate limiter...");
        //获取当前时间
        long currentTimeStamp = System.currentTimeMillis();
        //超过一个执行周期
        if (currentTimeStamp - lastTimeStamp >= milliSeconds){
            //重置窗口开始时间
            lastTimeStamp = currentTimeStamp;
            //释放所有资源
            semaphore.release(currentCounter.get());
            //重置计数
            currentCounter.set(0);
            return true;
        }
        boolean result = semaphore.tryAcquire();
        //成功获取资源
        if (result){
            currentCounter.incrementAndGet();
        }
        return result;
    }

    /**
     * fix07
     * 在设计上，基于Semaphore的限流策略，要实现的逻辑就是在一段时间内，最多允许通过多少个请求，如果请求的数量在这段时间内，达到了上限，就需要触发限流的操作。
     * 但是，在实际实现过程中，会出现在这段时间内，达到请求数量的上限时，没有触发限流的问题。
     *
     * 当请求获取到限流资源，执行完业务逻辑后，就会在finally代码块中调用release()方法释放资源，则后续的请求就会再次获取到限流资源，
     * 继续执行后续的业务逻辑，达不到限流的效果。
     *
     * 解决：
     * 在SemaphoreRateLimiterInvoker类的tryAcquire()方法中加入时间窗口的逻辑，在同一个时间窗口内最多允许通过permits个请求，不对semaphore进行释放资源的操作。
     * 当在同一个时间窗口内，请求数达到上限时，就会触发限流逻辑。在同一个时间窗口结束时，统一释放semaphore占用的资源。
     */
    @Override
    public void release() {
        //TODO ignore
    }
}
