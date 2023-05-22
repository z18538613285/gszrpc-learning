package io.gushizhao.rpc.test.spi.service;


import io.gushizhao.rpc.spi.annotation.SPI;

@SPI("spiService")
public interface SPIService {
    String hello(String name);
}
