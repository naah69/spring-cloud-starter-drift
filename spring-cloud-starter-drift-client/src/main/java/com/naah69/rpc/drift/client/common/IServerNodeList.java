package com.naah69.rpc.drift.client.common;

import java.util.LinkedHashSet;
import java.util.Map;

/**
 * 服务器节点列表接口
 * the interface of server node list
 *
 * @param <T>
 * @author naah
 */
public interface IServerNodeList<T extends DriftServerNode> {

    /**
     * 初始化服务列表
     *
     * @return
     */
    Map<String, LinkedHashSet<T>> getInitialListOfThriftServers();

    /**
     * 刷新服务列表
     *
     * @return
     */
    Map<String, LinkedHashSet<T>> getUpdatedListOfThriftServers();
}
