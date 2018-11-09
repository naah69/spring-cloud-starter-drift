package com.naah69.rpc.drift.client.exception;

/**
 * 客户端异常
 *
 * @author naah
 */
public class DriftClientException extends RuntimeException {

    public DriftClientException(String message) {
        super(message);
    }

    public DriftClientException(String message, Throwable t) {
        super(message, t);
    }
}
