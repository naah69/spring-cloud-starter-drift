package com.naah69.rpc.drift.client.cache;

import com.google.common.collect.Maps;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 服务方法缓存
 *
 * @author naah
 */
public class DriftServiceMethodCache {

    private Map<String, Method> methodCachedMap = Maps.newHashMap();

    private final Class<?> cacheClass;

    public DriftServiceMethodCache(Class<?> cacheClass) {
        this.cacheClass = cacheClass;
        Method[] declaredMethods = cacheClass.getDeclaredMethods();
        List<String> nonCachedMethods = new ArrayList<>();

        for (Method method : declaredMethods) {

            /**
             *安全检查 && public && 不是静态的
             */
            if (!method.isAccessible() && ((1 & method.getModifiers()) > 0) && ((8 & method.getModifiers()) == 0)) {
                put(method);
                nonCachedMethods.add(method.getName());
            }
        }

        for (String methodName : nonCachedMethods) {
            methodCachedMap.remove(methodName);
        }
    }

    /**
     * 方法加入缓存
     *
     * @param method
     */
    private void put(Method method) {
        Type[] types = method.getParameterTypes();
        /**
         *key为 方法名+参数数量+所有参数名
         */
        StringBuilder cachedKey = new StringBuilder(method.getName() + types.length);
        for (Type type : types) {
            String typeName = type.toString();
            if (typeName.startsWith("class ")) {
                typeName = typeName.substring(6);
            }
            cachedKey.append(typeName);
        }
        methodCachedMap.put(cachedKey.toString(), method);
    }

    /**
     * 获取方法
     *
     * @param name      方法名
     * @param arguments 参数列表
     * @return
     * @throws NoSuchMethodException
     */
    public Method getMethod(String name, Class<?>... arguments) throws NoSuchMethodException {
        Method method = methodCachedMap.get(name);
        if (method == null) {
            StringBuilder nameBuilder = new StringBuilder(name + arguments.length);
            for (Class<?> argument : arguments) {
                nameBuilder.append(argument.getName());
            }
            name = nameBuilder.toString();
            method = methodCachedMap.get(name);
        }
        return method;
    }

    public Class<?> getCacheClass() {
        return cacheClass;
    }
}
