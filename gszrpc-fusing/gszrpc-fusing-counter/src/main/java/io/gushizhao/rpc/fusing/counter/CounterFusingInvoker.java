package io.gushizhao.rpc.fusing.counter;

import io.gushizhao.rpc.constants.RpcConstants;
import io.gushizhao.rpc.fusing.base.AbstractFusingInvoker;
import io.gushizhao.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/30 15:23
 */
@SPIClass
public class CounterFusingInvoker extends AbstractFusingInvoker {

    private final Logger logger = LoggerFactory.getLogger(CounterFusingInvoker.class);

    @Override
    public double getFailureStrategyValue() {
        return currentFailureCounter.doubleValue();
    }

    @Override
    public boolean invokeFusingStrategy() {
        boolean result = false;
        switch (fusingStatus.get()){
            //关闭状态
            case RpcConstants.FUSING_STATUS_CLOSED:
                result =  this.invokeClosedFusingStrategy();
                break;
            //半开启状态
            case RpcConstants.FUSING_STATUS_HALF_OPEN:
                result = this.invokeHalfOpenFusingStrategy();
                break;
            //开启状态
            case RpcConstants.FUSING_STATUS_OPEN:
                result = this.invokeOpenFusingStrategy();
                break;
            default:
                result = this.invokeClosedFusingStrategy();
                break;
        }
        logger.info("execute counter fusing strategy, current fusing status is {}", fusingStatus.get());
        return result;
    }


}
