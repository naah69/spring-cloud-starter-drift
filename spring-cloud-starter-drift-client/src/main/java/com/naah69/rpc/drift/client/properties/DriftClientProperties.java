package com.naah69.rpc.drift.client.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author naah
 */
@ConfigurationProperties(prefix = "spring.drift.client")
public class DriftClientProperties {


    private String transportModel = DriftTransportModel.TRANSPORT_MODEL_DEFAULT;
    private String protocolModel = DriftProtocolModel.PROTOCOL_MODEL_DEFAULT;

    /**
     * 扫描包路径
     */
    private String packageToScan = DEFAULT_PACKAGE_TO_SCAN;

    private Long refreshInterval = DEFAULT_REFRESH_INTERVAL;

    /**
     * 连接池配置信息
     */
    private DriftClientPoolProperties pool;

    /**
     * 客户端扫描的包名称/多个子包用逗号分割
     */
    private final static String DEFAULT_PACKAGE_TO_SCAN = "";

    /**
     * 服务列表默认刷新时间
     */
    private final static Long DEFAULT_REFRESH_INTERVAL = 30000L;

    public String getTransportModel() {
        return transportModel;
    }

    public void setTransportModel(String transportModel) {
        this.transportModel = transportModel;
    }

    public String getProtocolModel() {
        return protocolModel;
    }

    public void setProtocolModel(String protocolModel) {
        this.protocolModel = protocolModel;
    }

    public String getPackageToScan() {
        return packageToScan;
    }

    public void setPackageToScan(String packageToScan) {
        this.packageToScan = packageToScan;
    }

    public DriftClientPoolProperties getPool() {
        return pool;
    }

    public void setPool(DriftClientPoolProperties pool) {
        this.pool = pool;
    }

    public Long getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(Long refreshInterval) {
        this.refreshInterval = refreshInterval;
    }
}
