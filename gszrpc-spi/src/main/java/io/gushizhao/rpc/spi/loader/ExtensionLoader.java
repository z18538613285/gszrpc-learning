package io.gushizhao.rpc.spi.loader;

import io.gushizhao.rpc.spi.annotation.SPI;
import io.gushizhao.rpc.spi.annotation.SPIClass;
import io.gushizhao.rpc.spi.factory.ExtensionFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/10 11:29
 */
public class ExtensionLoader<T> {

    private final Logger logger = LoggerFactory.getLogger(ExtensionLoader.class);

    public static final String SERVICES_DIRECTORY = "META-INF/services/";
    public static final String GUSHIZHAO_DIRECTORY = "META-INF/gushizhao/";
    public static final String GUSHIZHAO_DIRECTORY_EXTERNAL = "META-INF/gushizhao/external/";
    public static final String GUSHIZHAO_DIRECTORY_INTERNAL = "META-INF/gushizhao/internal/";

    private static final String[] SPI_DIRECTORIES = new String[]{
            SERVICES_DIRECTORY,
            GUSHIZHAO_DIRECTORY,
            GUSHIZHAO_DIRECTORY_EXTERNAL,
            GUSHIZHAO_DIRECTORY_INTERNAL
    };

    private static final Map<Class<?>, ExtensionLoader<?>> LOADERS = new ConcurrentHashMap<>();

    private final Class<T> clazz;

    private final ClassLoader classLoader;

    private final Holder<Map<String, Class<?>>> cachedClass = new Holder<>();

    private final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();

    private final Map<Class<?>, Object> spiClassInstances = new ConcurrentHashMap<>();

    private String cachedDefaultName;

    /**
     * Instantiates a new Extension loader
     * @param clazz
     * @param classLoader
     */
    public ExtensionLoader(final Class<T> clazz, final ClassLoader classLoader) {
        this.clazz = clazz;
        this.classLoader = classLoader;
        if (!Objects.equals(clazz, ExtensionFactory.class)) {
            ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getExtensionClasses();
        }
    }

    /**
     * get extension loader
     *
     * @param clazz
     * @param classLoader
     * @param <T>
     * @return
     */
    public static <T> ExtensionLoader<T> getExtensionLoader(final Class<T> clazz, final ClassLoader classLoader) {
        Objects.requireNonNull(clazz, "extension clazz is null");

        if (!clazz.isInterface()) {
            throw new IllegalArgumentException("extension clazz(" + clazz + ") is not interface!");
        }
        if (!clazz.isAnnotationPresent(SPI.class)) {
            throw new IllegalArgumentException("extension clazz(" + clazz + ") without @" + SPI.class + " Annotation");
        }
        ExtensionLoader<T> extensionLoader = (ExtensionLoader<T>) LOADERS.get(clazz);
        if (Objects.nonNull(extensionLoader)) {
            return extensionLoader;
        }
        LOADERS.putIfAbsent(clazz, new ExtensionLoader<>(clazz, classLoader));
        return (ExtensionLoader<T>) LOADERS.get(clazz);
    }

    /**
     * 直接获取想要的类实现
     *
     * @param clazz 接口的Class实例
     * @param name  SPI名称
     * @param <T>
     * @return
     */
    public static <T> T  getExtension(final Class<T> clazz, String name) {
        return StringUtils.isEmpty(name) ? getExtensionLoader(clazz).getDefaultSpiClassInstance() : getExtensionLoader(clazz).getSpiClassInstance(name);
    }


    /**
     * get extension loader
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> ExtensionLoader<T> getExtensionLoader(final Class<T> clazz) {
        return getExtensionLoader(clazz, ExtensionLoader.class.getClassLoader());
    }

    /**
     * Get default spi class instance
     * @return
     */
    public T getDefaultSpiClassInstance() {
        getExtensionClasses();
        if (StringUtils.isBlank(cachedDefaultName)) {
            return null;
        }
        return getSpiClassInstance(cachedDefaultName);
    }

    /**
     * Gets spi class
     * @param name
     * @return
     */
    public T getSpiClassInstance(final String name) {
        if (StringUtils.isBlank(name)) {
            throw new NullPointerException("get spi class name is null");
        }
        Holder<Object> objectHolder = cachedInstances.get(name);
        if (Objects.isNull(objectHolder)) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            objectHolder = cachedInstances.get(name);
        }
        Object value = objectHolder.getValue();
        if (Objects.isNull(value)) {
            synchronized (cachedInstances) {
                value = objectHolder.getValue();
                if (Objects.isNull(value)) {
                    value = createExtension(name);
                    objectHolder.setValue(value);
                }
            }
        }
        return (T) value;
    }


    /**
     * get all spi class spi
     * @return
     */
    public List<T> getSpiClassInstances() {
        Map<String, Class<?>> extensionClasses = this.getExtensionClasses();
        if (extensionClasses.isEmpty()) {
            return Collections.emptyList();
        }
        if (Objects.equals(extensionClasses.size(), cachedInstances.size())) {
            return (List<T>) this.cachedInstances.values().stream().map(e ->{
                return e.getValue();
            }).collect(Collectors.toList());
        }
        List<T> instances = new ArrayList<>();
        extensionClasses.forEach((name, v) -> {
            T instance = this.getSpiClassInstance(name);
            instances.add(instance);
        });
        return instances;
    }

    private T createExtension(final String name) {
        Class<?> aClass = getExtensionClasses().get(name);
        if (Objects.isNull(aClass)) {
            throw new IllegalArgumentException("name is error");
        }
        Object o = spiClassInstances.get(aClass);
        if (Objects.isNull(o)) {
            try {
                spiClassInstances.putIfAbsent(aClass, aClass.newInstance());
                o = spiClassInstances.get(aClass);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException("Extension instance(name: " + name + ", class: "
                        + aClass + ") could not be instantiated: " + e.getMessage(), e);
            }
        }
        return (T) o;
    }

    /**
     * Gets extension classes
     * @return
     */
    public Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = cachedClass.getValue();
        if (Objects.isNull(classes)) {
            synchronized (cachedClass) {
                classes = cachedClass.getValue();
                if (Objects.isNull(classes)) {
                    classes = loadExtensionClass();
                    cachedClass.setValue(classes);
                }
            }
        }
        return classes;
    }

    private Map<String, Class<?>> loadExtensionClass() {
        SPI annotation = clazz.getAnnotation(SPI.class);
        if (Objects.nonNull(annotation)) {
            String value = annotation.value();
            if (StringUtils.isNotBlank(value)) {
                cachedDefaultName = value;
            }
        }
        Map<String, Class<?>> classes = new HashMap<>(16);
        loadDirectory(classes);
        return classes;
    }

    private void loadDirectory(final Map<String, Class<?>> classes) {
        for (String directory : SPI_DIRECTORIES) {
            String fileName = directory + clazz.getName();
            try {
                Enumeration<URL> urls = Objects.nonNull(this.classLoader) ? classLoader.getResources(fileName) : ClassLoader.getSystemResources(fileName);
                if (Objects.nonNull(urls)) {
                    while (urls.hasMoreElements()) {
                        URL url = urls.nextElement();
                        loadResources(classes, url);
                    }
                }
            } catch (IOException t) {
                logger.error("load extension class error {}", fileName, t);
            }
        }
    }

    private void loadResources(final Map<String, Class<?>> classes, final URL url) {
        try (InputStream inputStream = url.openStream()){
            Properties properties = new Properties();
            properties.load(inputStream);
            properties.forEach((k, v) -> {
                String name = (String) k;
                String classPath = (String) v;
                if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(classPath)) {
                    try {
                        loadClass(classes, name, classPath);
                    } catch (ClassNotFoundException e) {
                        throw new IllegalStateException("load extension resources error", e);
                    }
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException("load extension resources error", e);
        }
    }

    private void loadClass(final Map<String, Class<?>> classes, final String name, final String classPath) throws ClassNotFoundException{
        Class<?> subClass = Objects.nonNull(this.classLoader) ? Class.forName(classPath, true, this.classLoader) : Class.forName(classPath);
        if (!clazz.isAssignableFrom(subClass)) {
            throw new IllegalStateException("load extension resources error," + subClass + " subtype is not of " + clazz);
        }
        if (!subClass.isAnnotationPresent(SPIClass.class)) {
            throw new IllegalStateException("load extension resources error," + subClass + " without @" + SPIClass.class + " annotation");
        }
        Class<?> oldClass = classes.get(name);
        if (Objects.isNull(oldClass)) {
            classes.put(name, subClass);
        } else if (!Objects.equals(oldClass, subClass)) {
            throw new IllegalStateException("load extension resources error,Duplicate class " + clazz.getName() + " name " + name + " on " + oldClass.getName() + " or " + subClass.getName());
        }
    }


    public static class Holder<T> {

        private volatile T value;

        public T getValue() {
            return value;
        }

        public void setValue(final T value) {
            this.value = value;
        }

    }
}
