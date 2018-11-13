package com.naah69.rpc.drift.client.exception;

/**
 * 客户端开启异常
 * client open exception
 *
 * @author naah
 */
public class DriftClientOpenException extends RuntimeException {

    public DriftClientOpenException(String message) {
        super(message);
    }

    public DriftClientOpenException(String message, Throwable cause) {
        super(message, cause);
    }

}
