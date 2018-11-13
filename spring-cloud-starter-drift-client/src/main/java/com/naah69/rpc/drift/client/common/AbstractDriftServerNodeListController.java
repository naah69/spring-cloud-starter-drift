package com.naah69.rpc.drift.client.common;

import com.google.common.collect.Maps;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * 抽象服务器节点类列表控制器
 * abstract Class of DriftServerNodeListController
 *
 * @author naah
 */
public abstract class AbstractDriftServerNodeListController<T extends DriftServerNode> implements IServerNodeList<T> {

    protected Map<String, LinkedHashSet<T>> serverNodeMap = Maps.newConcurrentMap();

    /**
     * 获取服务器节点Map
     * get the Map of server node
     *
     * @return
     */
    public Map<String, LinkedHashSet<T>> getServerNodeMap() {
        return serverNodeMap;
    }

    /**
     * 初始化Thrift服务器Map
     * initial the Map of thrift servers
     *
     * @return
     */
    @Override
    public Map<String, LinkedHashSet<T>> getInitialListOfThriftServers() {
        return getThriftServers();
    }

    /**
     * 刷新Thrift服务器Map
     * update the Map of thrift servers
     *
     * @return
     */
    @Override
    public Map<String, LinkedHashSet<T>> getUpdatedListOfThriftServers() {
        return refreshThriftServers();
    }

    /**
     * 通过serviceName获取节点列表
     * get Thrift server node List by service name
     *
     * @param serviceName format is serviceId$version or serviceId
     * @return
     */
    public abstract List<T> getThriftServer(String serviceName);

    /**
     * 通过serviceName刷新节点列表
     * refresh Thrift server node list by service name
     *
     * @param serviceName format is serviceId$version or serviceId
     * @return
     */
    public abstract List<T> refreshThriftServer(String serviceName);

    /**
     * 获取所有服务节点列表
     * get the Map that is contains all server node of all Thrift server
     *
     * @return
     */
    public abstract Map<String, LinkedHashSet<T>> getThriftServers();

    /**
     * 刷新所有节点服务列表
     * refresh the Map that is contains all server node of all Thrift server
     *
     * @return
     */
    public abstract Map<String, LinkedHashSet<T>> refreshThriftServers();

}
