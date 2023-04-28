package io.gushizhao.rpc.common.scanner.server;

import io.gushizhao.rpc.annotation.RpcService;
import io.gushizhao.rpc.common.helper.RpcServiceHelper;
import io.gushizhao.rpc.common.scanner.ClassScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author huzhichao
 * @Description 实现 @RpcService 注解的扫描器
 * @Date 2023/4/21 17:02
 */
public class RpcServiceScanner extends ClassScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServiceScanner.class);

    /**
     * 扫描指定包下的类，并筛选使用 @RpcService 注解标注的类
     *
     * @return
     * @throws Exception
     */
    public static Map<String, Object> doScannerWithRpcServiceAnnotationFilterAndRegistryService(
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
                RpcService rpcService = clazz.getAnnotation(RpcService.class);
                if (rpcService != null) {
                    // 优先使用 interfaceClass，如果为空再使用interfaceClassName
                    // TODO 后续逻辑向注册中心注册服务元数据，同时向 handleMap 中记录标注了 RpcService 注解的类实例
                    // handleMap 中的 key 先简单的存储为 serviceName+version+group ，后续根据实际情况处理 key
                    String serviceName = getServiceName(rpcService);
                    //String key = serviceName.concat(rpcService.version()).concat(rpcService.group());
                    String key = RpcServiceHelper.buildServiceKey(serviceName, rpcService.version(), rpcService.group());
                    handleMap.put(key, clazz.newInstance());

                  /*  LOGGER.info("当前标注了@RpcService注解的类实例名称===>>>" + clazz.getName());
                    LOGGER.info("@RpcService注解上标注的属性信息如下：");
                    LOGGER.info("interfaceClass===>>>" + rpcService.interfaceClass().getName());
                    LOGGER.info("interfaceClassName===>>>" + rpcService.interfaceClassName());
                    LOGGER.info("version===>>>" + rpcService.version());
                    LOGGER.info("group===>>>" + rpcService.group());*/
                }
            } catch (Exception e) {
                LOGGER.error("scann classes throws exception:{}", e);
            }
        });
        return handleMap;
    }

    /**
     * 获取serviceName
     */
    private static String getServiceName(RpcService rpcService){
        //优先使用interfaceClass
        Class clazz = rpcService.interfaceClass();
        if (clazz == null || clazz == void.class){
            return rpcService.interfaceClassName();
        }
        String serviceName = clazz.getName();
        if (serviceName == null || serviceName.trim().isEmpty()){
            serviceName = rpcService.interfaceClassName();
        }
        return serviceName;
    }
}
