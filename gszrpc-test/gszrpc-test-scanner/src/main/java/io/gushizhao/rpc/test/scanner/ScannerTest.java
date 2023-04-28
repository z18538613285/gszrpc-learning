package io.gushizhao.rpc.test.scanner;

import io.gushizhao.rpc.common.scanner.ClassScanner;
import io.gushizhao.rpc.common.scanner.reference.RpcReferenceScanner;
import io.gushizhao.rpc.common.scanner.server.RpcServiceScanner;
import org.junit.Test;

import java.util.List;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/4/21 17:59
 */

public class ScannerTest {

    /**
     * 扫描 io.gushizhao.rpc.test.scanner 包下所有的类
     * @throws Exception
     */
    @Test
    public void testScannerClassNameList() throws Exception {
        List<String> classNameList = ClassScanner.getClassNameList("io.gushizhao.rpc.test.scanner");
        classNameList.forEach(System.out::println);
    }

    /**
     * 扫描 io.gushizhao.rpc.test.scanner 包下所有标注了 @RpcService 注解的类
     * @throws Exception
     */
    @Test
    public void testScannerClassNameListByRpcService() throws Exception {
        RpcServiceScanner.doScannerWithRpcServiceAnnotationFilterAndRegistryService("io.gushizhao.rpc.test.scanner");
    }

    /**
     * 扫描 io.gushizhao.rpc.test.scanner 包下所有标注了 @RpcService 注解的类
     * @throws Exception
     */
    @Test
    public void testScannerClassNameListByRpcReference() throws Exception {
        RpcReferenceScanner.doScannerWithRpcReferenceAnnotationFilterAndRegistryService("io.gushizhao.rpc.test.scanner");
    }
}
