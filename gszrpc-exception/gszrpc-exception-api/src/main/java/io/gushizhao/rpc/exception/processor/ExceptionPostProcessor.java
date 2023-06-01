package io.gushizhao.rpc.exception.processor;

import io.gushizhao.rpc.constants.RpcConstants;
import io.gushizhao.rpc.spi.annotation.SPI;

@SPI(RpcConstants.EXCEPTION_POST_PROCESSOR_PRINT)
public interface ExceptionPostProcessor {

    void postExceptionProcessor(Throwable e);
}
