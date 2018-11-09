package com.naah69.rpc.drift.client.discovery;

/**
 * 注册中心枚举类
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
     * Spring Cloud Discovery Client 描述
     */
    private final String description;

    RegistryEnum(String description) {
        this.description = description;
    }

}
