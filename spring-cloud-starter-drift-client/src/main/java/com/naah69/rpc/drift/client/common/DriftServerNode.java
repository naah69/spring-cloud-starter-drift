package com.naah69.rpc.drift.client.common;

/**
 * 服务器节点类
 * server node
 *
 * @author naah
 */
public class DriftServerNode {

    private String host;

    private int port;

    private int timeout;

    public DriftServerNode() {
    }

    public DriftServerNode(String host, int port) {
        this.host = host;
        this.port = port;
    }


    public DriftServerNode(String host, int port, int timeout) {
        this(host, port);
        this.timeout = timeout;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DriftServerNode that = (DriftServerNode) o;

        if (port != that.port) {
            return false;
        }
        return host != null ? host.equals(that.host) : that.host == null;
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }
}
