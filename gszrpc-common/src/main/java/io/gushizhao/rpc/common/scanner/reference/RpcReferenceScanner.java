package io.gushizhao.rpc.common.scanner.reference;

import io.gushizhao.rpc.annotation.RpcReference;
import io.gushizhao.rpc.common.scanner.ClassScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @Author huzhichao
 * @Description 实现@RpcReference注解的扫描器
 * @Date 2023/4/21 17:25
 */
public class RpcReferenceScanner extends ClassScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcReferenceScanner.class);

    /**
     * 扫描指定包下的类，并筛选使用 @RpcReferenceScanner 注解标注的类
     * @return
     * @throws Exception
     */
    public static Map<String, Object> doScannerWithRpcReferenceAnnotationFilterAndRegistryService(
            /*String host,
            int port,*/
            String scanPackage/*,
            RegistryService registryService*/
    ) throws Exception {
        Map<String, Object> handleMap = new HashMap<>();
        List<String> classNameList = getClassNameList(scanPackage);
        if (classNameList == null || classNameList.isEmpty()) {
            return handleMap;
        }
        classNameList.stream().forEach((className) -> {
            try {
                Class<?> clazz = Class.forName(className);
                Field[] declaredFields = clazz.getDeclaredFields();
                Stream.of(declaredFields).forEach((field) -> {
                    RpcReference rpcReference = field.getAnnotation(RpcReference.class);
                    if (rpcReference != null) {
                        // 优先使用 interfaceClass，如果为空再使用interfaceClassName
                        // TODO 后续逻辑,将@RpcReference 注解标注的接口引用代理对象，放入全局缓存中

                        LOGGER.info("当前标注了@RpcReference注解的字段名称===>>>" + field.getName());
                        LOGGER.info("@RpcReference注解上标注的属性信息如下：");
                        LOGGER.info("version===>>>" + rpcReference.version());
                        LOGGER.info("group===>>>" + rpcReference.group());
                        LOGGER.info("registryType===>>>" + rpcReference.registryType());
                        LOGGER.info("registryAddress===>>>" + rpcReference.registryAddress());
                    }
                });
            } catch (Exception e) {
                LOGGER.error("scann classes throws exception:{}", e);
            }
        });
        return handleMap;
    }
}
