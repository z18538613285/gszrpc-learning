package io.gushizhao.rpc.consumer.spring;

import io.gushizhao.rpc.annotation.RpcReference;
import io.gushizhao.rpc.constants.RpcConstants;
import io.gushizhao.rpc.consumer.spring.context.RpcConsumerSpringContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/19 16:30
 */
@Component
public class RpcConsumerPostProcessor implements ApplicationContextAware, BeanClassLoaderAware, BeanFactoryPostProcessor {

    private final Logger logger = LoggerFactory.getLogger(RpcConsumerPostProcessor.class);

    private ApplicationContext context;
    private ClassLoader classLoader;

    private final Map<String, BeanDefinition> rpcRefBeanDefinitions = new LinkedHashMap<>();

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * 在执行的过程中，SpringBootConsumerAutoConfiguration类下的rpcClient()方法会在RpcConsumerPostProcessor类的postProcessBeanFactory()方法之后运行，
     * 就会导致服务消费者服务yml文件中的值覆盖掉解析的@RpcReference注解中的值。
     * @param beanFactory
     * @throws BeansException
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        for (String beanDefinitionName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);
            String beanClassName = beanDefinition.getBeanClassName();
            if (beanClassName != null) {
                Class<?> clazz = ClassUtils.resolveClassName(beanClassName, this.classLoader);
                ReflectionUtils.doWithFields(clazz, this::parseRpcReference);
            }
        }
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        this.rpcRefBeanDefinitions.forEach((beanName, beanDefinition) -> {
            if (context.containsBean(beanName)) {
                throw new IllegalArgumentException("spring context already has a bean named " + beanName);
            }
            registry.registerBeanDefinition(beanName, rpcRefBeanDefinitions.get(beanName));
            logger.info("registered RpcReferenceBean {} success.", beanName);
        });
    }

    private void parseRpcReference(Field field) {
        RpcReference annotation = AnnotationUtils.getAnnotation(field, RpcReference.class);
        if (annotation != null) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RpcReferenceBean.class);
            builder.setInitMethodName(RpcConstants.INIT_METHOD_NAME);
            builder.addPropertyValue("interfaceClass", field.getType());
            builder.addPropertyValue("version", annotation.version());
            builder.addPropertyValue("registryType", annotation.registryType());
            builder.addPropertyValue("registryAddress", annotation.registryAddress());
            builder.addPropertyValue("loadBalanceType", annotation.loadBalanceType());
            builder.addPropertyValue("serializationType", annotation.serializationType());
            builder.addPropertyValue("timeout", annotation.timeout());
            builder.addPropertyValue("async", annotation.async());
            builder.addPropertyValue("oneway", annotation.onwway());
            builder.addPropertyValue("proxy", annotation.proxy());
            builder.addPropertyValue("group", annotation.group());
            builder.addPropertyValue("scanNotActiveChannelInterval", annotation.scanNotActiveChannelInterval());
            builder.addPropertyValue("heartbeatInterval", annotation.heartbeatInterval());
            builder.addPropertyValue("retryInterval", annotation.retryInterval());
            builder.addPropertyValue("retryTimes", annotation.retryTimes());
            builder.addPropertyValue("enableResultCache", annotation.enableResultCache());
            builder.addPropertyValue("resultCacheExpire", annotation.resultCacheExpire());
            builder.addPropertyValue("enableDirectServer", annotation.enableDirectServer());
            builder.addPropertyValue("directServerUrl", annotation.directServerUrl());
            builder.addPropertyValue("enableDelayConnection", annotation.enableDelayConnection());
            builder.addPropertyValue("corePoolSize", annotation.corePoolSize());
            builder.addPropertyValue("maximumPoolSize", annotation.maximumPoolSize());
            builder.addPropertyValue("flowType", annotation.flowType());
            builder.addPropertyValue("enableBuffer", annotation.enableBuffer());
            builder.addPropertyValue("bufferSize", annotation.bufferSize());
            builder.addPropertyValue("reflectType", annotation.reflectType());
            builder.addPropertyValue("fallbackClassName", annotation.fallbackClassName());
            builder.addPropertyValue("fallbackClass", annotation.fallbackClass());
            builder.addPropertyValue("enableRateLimiter", annotation.enableRateLimiter());
            builder.addPropertyValue("rateLimiterType", annotation.rateLimiterType());
            builder.addPropertyValue("permits", annotation.permits());
            builder.addPropertyValue("milliSeconds", annotation.milliSeconds());
            builder.addPropertyValue("rateLimiterFailStrategy", annotation.rateLimiterFailStrategy());
            builder.addPropertyValue("enableFusing", annotation.enableFusing());
            builder.addPropertyValue("fusingType", annotation.fusingType());
            builder.addPropertyValue("totalFailure", annotation.totalFailure());
            builder.addPropertyValue("fusingMilliSeconds", annotation.fusingMilliSeconds());
            builder.addPropertyValue("exceptionPostProcessorType", annotation.exceptionPostProcessorType());
            BeanDefinition beanDefinition = builder.getBeanDefinition();
            rpcRefBeanDefinitions.put(field.getName(), beanDefinition);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
        RpcConsumerSpringContext.getInstance().setContext(applicationContext);
    }
}
