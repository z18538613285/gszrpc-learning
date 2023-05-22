//package io.gushizhao.rpc.test.registry;
//
//import io.gushizhao.rpc.protocol.meta.ServiceMeta;
//import io.gushizhao.rpc.registry.api.RegistryService;
//import io.gushizhao.rpc.registry.api.config.RegistryConfig;
//import io.gushizhao.rpc.registry.zookeeper.ZookeeperRegistryService;
//import org.junit.Before;
//import org.junit.Test;
//
///**
// * @Author huzhichao
// * @Description TODO
// * @Date 2023/5/8 16:59
// */
//public class ZookeeperRegistryTest {
//
//    private RegistryService registryService;
//    private ServiceMeta serviceMeta;
//
//    @Before
//    public void init() throws Exception {
//        RegistryConfig registryConfig = new RegistryConfig("127.0.0.1:2181", "zookeeper");
//        this.registryService = new ZookeeperRegistryService();
//        this.registryService.init(registryConfig);
//        this.serviceMeta = new ServiceMeta(ZookeeperRegistryTest.class.getName(), "1.0.0", "127.0.0.1", 8080, "gushizhao");
//    }
//
//
//    @Test
//    public void testRegistry() throws Exception {
//        this.registryService.register(serviceMeta);
//    }
//
//    @Test
//    public void testUnRegistry() throws Exception {
//        this.registryService.unRegister(serviceMeta);
//    }
//
//    @Test
//    public void testDiscovery() throws Exception {
//        this.registryService.discovery(RegistryService.class.getName(), "gushizhao".hashCode());
//    }
//    @Test
//    public void testDestroy() throws Exception {
//        this.registryService.destroy();
//    }
//
//}
