package io.gushizhao.rpc.exception.processor.print;

import io.gushizhao.rpc.exception.processor.ExceptionPostProcessor;
import io.gushizhao.rpc.spi.annotation.SPIClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/30 17:40
 */
@SPIClass
public class PrintExceptionPostProcessor implements ExceptionPostProcessor {
    private final Logger logger = LoggerFactory.getLogger(PrintExceptionPostProcessor.class);


    @Override
    public void postExceptionProcessor(Throwable e) {
        logger.info("程序抛出异常===>>>" + e.getMessage(), e);
    }
}
