package com.naah69.rpc.drift.client.discovery.consul;

import com.naah69.rpc.drift.client.common.DriftServerNode;

import java.util.List;

/**
 * Consul服务节点
 *
 * @author naah
 */
public class DriftConsulServerNode extends DriftServerNode {

    private String node;

    private String serviceId;

    private List<String> tags;

    private String host;

    private int port;

    private String address;

    private boolean isHealth;

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isHealth() {
        return isHealth;
    }

    public void setHealth(boolean health) {
        isHealth = health;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        DriftConsulServerNode that = (DriftConsulServerNode) o;

        if (getPort() != that.getPort()) {
            return false;
        }
        return getHost() != null ? getHost().equals(that.getHost()) : that.getHost() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getHost() != null ? getHost().hashCode() : 0);
        result = 31 * result + getPort();
        return result;
    }

    @Override
    public String toString() {
        return "DriftServerNode{" +
                "node='" + node + '\'' +
                ", serviceId='" + serviceId + '\'' +
                ", tags=" + tags +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", address='" + address + '\'' +
                ", isHealth=" + isHealth +
                '}';
    }

}
