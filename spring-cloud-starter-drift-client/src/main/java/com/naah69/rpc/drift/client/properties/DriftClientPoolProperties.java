package com.naah69.rpc.drift.client.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author naah
 */
@ConfigurationProperties("spring.drift.client.pool")
public class DriftClientPoolProperties {

    /**
     * 重试次数
     */
    private int retryTimes = 3;

    /**
     * 连接超时
     */
    private int connectTimeout = 10000;

    /**
     * 请求超时
     */
    private int requestTimeout = 10000;

    /**
     * 连接池最大数
     */
    private int poolMaxTotalPerKey = 60;

    /**
     * 连接池最大空闲数
     */
    private int poolMaxIdlePerKey = 40;

    /**
     * 连接池最小空闲数
     */
    private int poolMinIdlePerKey = 3;

    /**
     * 连接池最大等待时间
     */
    private long poolMaxWait = 180000;

    /**
     * 池对象创建时时验证是否正常可用
     */
    private boolean testOnCreate = true;

    /**
     * 池对象借出时验证是否正常可用
     */
    private boolean testOnBorrow = true;


    /**
     * 池对象归还时验证是否正常可用
     */
    private boolean testOnReturn = true;

    /**
     * 空闲连接自动被空闲连接回收器
     */
    private boolean isTestWhileIdle = true;

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getPoolMaxTotalPerKey() {
        return poolMaxTotalPerKey;
    }

    public void setPoolMaxTotalPerKey(int poolMaxTotalPerKey) {
        this.poolMaxTotalPerKey = poolMaxTotalPerKey;
    }

    public int getPoolMaxIdlePerKey() {
        return poolMaxIdlePerKey;
    }

    public void setPoolMaxIdlePerKey(int poolMaxIdlePerKey) {
        this.poolMaxIdlePerKey = poolMaxIdlePerKey;
    }

    public int getPoolMinIdlePerKey() {
        return poolMinIdlePerKey;
    }

    public void setPoolMinIdlePerKey(int poolMinIdlePerKey) {
        this.poolMinIdlePerKey = poolMinIdlePerKey;
    }

    public long getPoolMaxWait() {
        return poolMaxWait;
    }

    public void setPoolMaxWait(long poolMaxWait) {
        this.poolMaxWait = poolMaxWait;
    }

    public boolean isTestOnCreate() {
        return testOnCreate;
    }

    public void setTestOnCreate(boolean testOnCreate) {
        this.testOnCreate = testOnCreate;
    }

    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }

    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public boolean isTestOnReturn() {
        return testOnReturn;
    }

    public void setTestOnReturn(boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
    }

    public boolean isTestWhileIdle() {
        return isTestWhileIdle;
    }

    public void setTestWhileIdle(boolean testWhileIdle) {
        isTestWhileIdle = testWhileIdle;
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }
}
