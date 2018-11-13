package com.naah69.rpc.drift.client.discovery;

/**
 * 注册中心枚举类
 * enum of registry center
 *
 * @author naah
 * @date 2018-11-03 1:43 PM
 * @desc
 */
public enum RegistryEnum {

    /**
     * Consul
     */
    SpringCloudConsulDiscoveryClient("SpringCloudConsulDiscoveryClient");

    /**
     * description：
     * example:Spring Cloud Discovery Client
     */
    private final String description;

    RegistryEnum(String description) {
        this.description = description;
    }

}
