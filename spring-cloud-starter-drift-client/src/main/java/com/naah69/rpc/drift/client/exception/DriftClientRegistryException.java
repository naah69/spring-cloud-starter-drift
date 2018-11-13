package com.naah69.rpc.drift.client.exception;

/**
 * 客户端注册异常
 * client registry exception
 *
 * @author naah
 */
public class DriftClientRegistryException extends RuntimeException {

    public DriftClientRegistryException(String message) {
        super(message);
    }

    public DriftClientRegistryException(String message, Throwable t) {
        super(message, t);
    }
}
