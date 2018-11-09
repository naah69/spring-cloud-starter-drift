package com.naah69.rpc.drift.client.exception;

/**
 * 客户端配置异常
 *
 * @author naah
 */
public class DriftClientConfigException extends RuntimeException {

    public DriftClientConfigException(String message) {
        super(message);
    }

    public DriftClientConfigException(String message, Throwable cause) {
        super(message, cause);
    }

}
