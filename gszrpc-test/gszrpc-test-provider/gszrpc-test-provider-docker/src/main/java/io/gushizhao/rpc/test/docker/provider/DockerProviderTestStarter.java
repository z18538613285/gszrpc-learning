package io.gushizhao.rpc.test.docker.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/22 15:39
 */
@SpringBootApplication
@ComponentScan(value = "io.gushizhao.rpc")
public class DockerProviderTestStarter {

    public static void main(String[] args) {
        SpringApplication.run(DockerProviderTestStarter.class, args);
    }
}
