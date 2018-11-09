package com.naah69.rpc.drift.client.utils;

import com.google.common.collect.Maps;
import com.naah69.rpc.drift.client.annotation.ThriftRefer;
import com.naah69.rpc.drift.client.common.DriftClientContext;
import com.naah69.rpc.drift.client.exception.DriftClientException;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * DriftAnnotationUtils
 *
 * @author naah
 * @date 2018-11-07 5:43 PM
 * @desc
 */
public class DriftAnnotationUtils {

    private static final Map<Class, ThriftRefer> THRIFT_REFER_CACHE = Maps.newConcurrentMap();

    /**
     * 通过截面方法获取调用方的ThriftRefer注解
     *
     * @param invocationMethod
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    public static ThriftRefer getThriftReferAnnotation(Method invocationMethod) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        DriftClientContext context = DriftClientContext.context();

        Class<?> sourceClass = null;
        boolean hasFind = false;
        for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
            if (stackTraceElement.getMethodName().contains(invocationMethod.getName())) {
                if (stackTraceElement.toString().contains("Proxy")) {
                    hasFind = true;
                    continue;
                }
            }
            if (hasFind) {
                String sourceClassName = (String) DriftReflectionUtils.getObjByFieldFromTarget("declaringClass", StackTraceElement.class, stackTraceElement);
                sourceClass = Class.forName(sourceClassName);
                break;
            }
        }

        if (sourceClass == null) {
            throw new DriftClientException("can't find Class of source");
        }


        ThriftRefer thriftReferAnnotation = THRIFT_REFER_CACHE.get(sourceClass);
        if (thriftReferAnnotation == null) {
            Field[] declaredFields = sourceClass.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                ThriftRefer annotation = AnnotationUtils.findAnnotation(declaredField, ThriftRefer.class);
                if (annotation != null) {
                    thriftReferAnnotation = annotation;
                    break;
                }
            }

            if (thriftReferAnnotation == null) {
                throw new DriftClientException("can't find the ThriftRefer annotation in " + sourceClass);
            }
        }


        return thriftReferAnnotation;
    }
}
