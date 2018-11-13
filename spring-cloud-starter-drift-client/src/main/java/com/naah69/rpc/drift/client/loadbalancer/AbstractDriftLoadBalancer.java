package com.naah69.rpc.drift.client.loadbalancer;

import com.naah69.rpc.drift.client.common.AbstractDriftServerNodeListController;
import com.naah69.rpc.drift.client.common.DriftServerNode;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * 负载均衡器抽象类
 * abstract class of load balancer
 *
 * @author naah
 */
public abstract class AbstractDriftLoadBalancer implements ILoadBalancer<DriftServerNode> {

    /**
     * 选择服务节点
     * choose server node
     *
     * @param key
     * @return
     */
    @Override
    public abstract DriftServerNode chooseServerNode(String key);

    /**
     * 获取所有服务节点Map
     * get all server node Map
     *
     * @return
     */
    @Override
    public Map<String, LinkedHashSet<DriftServerNode>> getAllServerNodes() {
        return getThriftServerNodeListController().getServerNodeMap();
    }

    /**
     * 刷新所有服务节点Map
     * refresh all server node Map
     *
     * @return
     */
    @Override
    public Map<String, LinkedHashSet<DriftServerNode>> getRefreshedServerNodes() {
        return getThriftServerNodeListController().refreshThriftServers();
    }

    /**
     * 通过serviceName获取服务节点列表
     * get server node by service name
     *
     * @param key
     * @return
     */
    @Override
    public List<DriftServerNode> getServerNodes(String key) {
        return getThriftServerNodeListController().getThriftServer(key);
    }

    /**
     * 通过serviceName刷新服务节点列表
     * refresh service name by service name
     *
     * @param key
     * @return
     */
    @Override
    public List<DriftServerNode> getRefreshedServerNodes(String key) {
        return getThriftServerNodeListController().refreshThriftServer(key);
    }

    /**
     * 获取服务节点列表控制器
     * get controller of server node list
     *
     * @return
     */
    public abstract AbstractDriftServerNodeListController getThriftServerNodeListController();

}
