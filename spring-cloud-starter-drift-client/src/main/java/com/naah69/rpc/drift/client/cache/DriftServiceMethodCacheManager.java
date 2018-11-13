package com.naah69.rpc.drift.client.cache;

import com.google.common.collect.Maps;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

/**
 * DriftServiceMethodCache管理器
 * the manager of {@link DriftServiceMethodCache}.
 *
 * @author naah
 */
public class DriftServiceMethodCacheManager {

    private static final Map<String, DriftServiceMethodCache> METHOD_CACHED_MAP = Maps.newConcurrentMap();

    /**
     * 获取缓存方法
     * get method from cache
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
     * 如果不存在就添加
     * if it absent,put it into cache
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
