package com.naah69.rpc.drift.client.utils;

/**
 * DriftThreadCacheUtils
 *
 * @author naah
 * @date 2018-11-08 11:52 AM
 * @desc
 */
public class DriftThreadCacheUtils {
    private static final ThreadLocal<Class> CLASS_THREAD_CACHE = new ThreadLocal<>();

    public static ThreadLocal<Class> getClassThreadCache() {
        return CLASS_THREAD_CACHE;
    }
}
