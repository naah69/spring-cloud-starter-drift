package com.naah69.rpc.drift.client.common;

import com.google.common.collect.Maps;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * 抽象服务节点类列表控制器
 *
 * @param <T>
 * @author naah
 */
public abstract class AbstractDriftServerNodeListController<T extends DriftServerNode> implements IServerNodeList<T> {

    protected Map<String, LinkedHashSet<T>> serverNodeMap = Maps.newConcurrentMap();

    /**
     * 获取服务节点列表
     *
     * @return
     */
    public Map<String, LinkedHashSet<T>> getServerNodeMap() {
        return serverNodeMap;
    }

    /**
     * 初始化服务列表
     *
     * @return
     */
    @Override
    public Map<String, LinkedHashSet<T>> getInitialListOfThriftServers() {
        return getThriftServers();
    }

    /**
     * 刷新服务列表
     *
     * @return
     */
    @Override
    public Map<String, LinkedHashSet<T>> getUpdatedListOfThriftServers() {
        return refreshThriftServers();
    }

    /**
     * 通过serviceName获取节点列表
     *
     * @param serviceName 服务名 serviceId$version 或 serviceId
     * @return
     */
    public abstract List<T> getThriftServer(String serviceName);

    /**
     * 通过serviceName刷新节点列表
     *
     * @param serviceName 服务名 serviceId$version 或 serviceId
     * @return
     */
    public abstract List<T> refreshThriftServer(String serviceName);

    /**
     * 获取所有服务节点列表
     *
     * @return
     */
    public abstract Map<String, LinkedHashSet<T>> getThriftServers();

    /**
     * 刷新所有节点服务列表
     *
     * @return
     */
    public abstract Map<String, LinkedHashSet<T>> refreshThriftServers();

}
