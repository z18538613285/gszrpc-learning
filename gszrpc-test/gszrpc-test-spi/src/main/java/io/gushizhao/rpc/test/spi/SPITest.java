package io.gushizhao.rpc.test.spi;

import io.gushizhao.rpc.spi.loader.ExtensionLoader;
import io.gushizhao.rpc.test.spi.service.SPIService;
import org.junit.Test;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/11 15:00
 */
public class SPITest {

    @Test
    public void testSpiLoader() {
        SPIService spiService = ExtensionLoader.getExtension(SPIService.class, "spiService");
        String result = spiService.hello("gushizhao");
        System.out.println(result);
    }
}
