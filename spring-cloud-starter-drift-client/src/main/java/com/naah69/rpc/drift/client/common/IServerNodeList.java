package com.naah69.rpc.drift.client.common;

import java.util.LinkedHashSet;
import java.util.Map;

/**
 * 服务节点列表端口
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
