package com.naah69.rpc.drift.client.common;

/**
 * 服务签名类
 * service signature
 *
 * @author naah
 */
public class DriftServiceSignature {

    private String thriftServiceId;

    private Class<?> thriftServiceClass;


    public DriftServiceSignature(String thriftServiceId, Class<?> thriftServiceClass) {
        this.thriftServiceId = thriftServiceId;
        this.thriftServiceClass = thriftServiceClass;
    }

    public String getThriftServiceId() {
        return thriftServiceId;
    }

    public void setThriftServiceId(String thriftServiceId) {
        this.thriftServiceId = thriftServiceId;
    }

    public Class<?> getThriftServiceClass() {
        return thriftServiceClass;
    }

    public void setThriftServiceClass(Class<?> thriftServiceClass) {
        this.thriftServiceClass = thriftServiceClass;
    }


    public String marker() {
        return String.join("$", new String[]{
                thriftServiceId, thriftServiceClass.getName()
        });
    }

}
