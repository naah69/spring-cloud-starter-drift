package com.naah69.rpc.drift.client.exception;

/**
 * 客户端实例化异常
 *
 * @author naah
 */
public class DriftClientInstantiateException extends RuntimeException {

    public DriftClientInstantiateException(String message) {
        super(message);
    }

    public DriftClientInstantiateException(String message, Throwable cause) {
        super(message, cause);
    }
}
