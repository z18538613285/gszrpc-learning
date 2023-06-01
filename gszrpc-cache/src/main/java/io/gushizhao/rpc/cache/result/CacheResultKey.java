package io.gushizhao.rpc.cache.result;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/22 17:20
 */
public class CacheResultKey implements Serializable {

    private static final long serialVersionUID = -354984654135458L;

    private long cacheTimeStamp;

    private String className;

    private String methodName;

    private Class<?>[] parameterTypes;

    private Object[] parameters;

    private String version;

    private String group;

    public CacheResultKey(String className, String methodName, Class<?>[] parameterTypes, Object[] parameters, String version, String group) {
        this.className = className;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.parameters = parameters;
        this.version = version;
        this.group = group;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CacheResultKey that = (CacheResultKey) o;
        return Objects.equals(className, that.className)
                && Objects.equals(methodName, that.methodName)
                && Arrays.equals(parameterTypes, that.parameterTypes)
                && Arrays.equals(parameters, that.parameters)
                && Objects.equals(version, that.version)
                && Objects.equals(group, that.group);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(className, methodName, version, group);
        result = 31 * result + Arrays.hashCode(parameterTypes);
        result = 31 * result + Arrays.hashCode(parameters);
        return result;
    }

    public long getCacheTimeStamp() {
        return cacheTimeStamp;
    }

    public void setCacheTimeStamp(long cacheTimeStamp) {
        this.cacheTimeStamp = cacheTimeStamp;
    }
}
