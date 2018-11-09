package com.naah69.rpc.drift.client.loadbalancer;

import com.naah69.rpc.drift.client.common.DriftServerNode;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * 负载均衡接口
 *
 * @param <T>
 * @author naah
 */
public interface ILoadBalancer<T extends DriftServerNode> {

    /**
     * 选择服务节点
     *
     * @param key
     * @return
     */
    T chooseServerNode(String key);

    /**
     * 获取所有的服务节点Map
     *
     * @return
     */
    Map<String, LinkedHashSet<T>> getAllServerNodes();

    /**
     * 刷新所有的服务节点Map
     *
     * @return
     */
    Map<String, LinkedHashSet<T>> getRefreshedServerNodes();

    /**
     * 通过serviceName获取服务节点列表
     *
     * @param key
     * @return
     */
    List<T> getServerNodes(String key);

    /**
     * 通过serviceName刷新服务节点列表
     *
     * @param key
     * @return
     */
    List<T> getRefreshedServerNodes(String key);

}
