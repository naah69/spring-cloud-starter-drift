package com.naah69.rpc.drift.client.utils;

import java.lang.reflect.Field;

/**
 * DriftReflectionUtils
 *
 * @author naah
 * @date 2018-11-03 3:49 PM
 * @desc
 */
public class DriftReflectionUtils {

    /**
     * 通过反射获取字段的值
     *
     * @param fieldName
     * @param targetClass
     * @param target
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public static Object getObjByFieldFromTarget(String fieldName, Class targetClass, Object target) throws NoSuchFieldException, IllegalAccessException {
        Field field = targetClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

}
