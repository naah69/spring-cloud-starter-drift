package com.naah69.rpc.drift.client.loadbalancer;

import com.naah69.rpc.drift.client.common.AbstractDriftServerNodeListController;
import com.naah69.rpc.drift.client.common.DriftServerNode;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * 负载均衡器抽象类
 *
 * @author naah
 */
public abstract class AbstractDriftLoadBalancer implements ILoadBalancer<DriftServerNode> {

    /**
     * 选择服务节点
     *
     * @param key
     * @return
     */
    @Override
    public abstract DriftServerNode chooseServerNode(String key);

    /**
     * 获取所有服务节点Map
     *
     * @return
     */
    @Override
    public Map<String, LinkedHashSet<DriftServerNode>> getAllServerNodes() {
        return getThriftServerNodeList().getServerNodeMap();
    }

    /**
     * 刷新所有服务节点Map
     *
     * @return
     */
    @Override
    public Map<String, LinkedHashSet<DriftServerNode>> getRefreshedServerNodes() {
        return getThriftServerNodeList().refreshThriftServers();
    }

    /**
     * 通过serviceName获取服务节点列表
     *
     * @param key
     * @return
     */
    @Override
    public List<DriftServerNode> getServerNodes(String key) {
        return getThriftServerNodeList().getThriftServer(key);
    }

    /**
     * 通过serviceName刷新服务节点列表
     *
     * @param key
     * @return
     */
    @Override
    public List<DriftServerNode> getRefreshedServerNodes(String key) {
        return getThriftServerNodeList().refreshThriftServer(key);
    }

    /**
     * 获取服务节点列表控制器
     *
     * @return
     */
    public abstract AbstractDriftServerNodeListController getThriftServerNodeList();

}
