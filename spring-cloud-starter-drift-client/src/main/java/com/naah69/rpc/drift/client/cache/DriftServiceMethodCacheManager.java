package com.naah69.rpc.drift.client.cache;

import com.google.common.collect.Maps;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

/**
 * Cache管理器 单例
 *
 * @author naah
 */
public class DriftServiceMethodCacheManager {

    private static final Map<String, DriftServiceMethodCache> METHOD_CACHED_MAP = Maps.newConcurrentMap();

    /**
     * 获取缓存方法
     *
     * @param targetClass
     * @param methodName
     * @param arguments
     * @return
     * @throws NoSuchMethodException
     */
    public static Method getMethod(Class<?> targetClass, String methodName, Class<?>... arguments) throws NoSuchMethodException {
        DriftServiceMethodCache methodCache = putIfAbsent(targetClass);
        return methodCache.getMethod(methodName, arguments);
    }

    public static void put(Class<?> targetClass) {
        DriftServiceMethodCache methodCache = new DriftServiceMethodCache(targetClass);
        METHOD_CACHED_MAP.put(targetClass.getName(), methodCache);
    }

    /**
     * 获取缓存
     *
     * @param targetClass
     * @return
     */
    public static DriftServiceMethodCache putIfAbsent(Class<?> targetClass) {
        String targetClassName = targetClass.getName();
        DriftServiceMethodCache methodCache = METHOD_CACHED_MAP.get(targetClassName);
        if (methodCache == null) {
            methodCache = new DriftServiceMethodCache(targetClass);
            DriftServiceMethodCache updateMethodCache = METHOD_CACHED_MAP.putIfAbsent(targetClassName, methodCache);
            if (Objects.nonNull(updateMethodCache)) {
                methodCache = updateMethodCache;
            }
        }
        return methodCache;
    }

}
