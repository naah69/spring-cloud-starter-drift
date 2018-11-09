package com.naah69.rpc.drift.client.exception;

/**
 * 客户端服务列表异常
 *
 * @author naah
 * @date 2018-11-03 2:42 PM
 * @desc
 */
public class DriftClientServerNodeListException extends RuntimeException {

    public DriftClientServerNodeListException(String message) {
        super(message);
    }

    public DriftClientServerNodeListException(String message, Throwable t) {
        super(message, t);
    }
}
