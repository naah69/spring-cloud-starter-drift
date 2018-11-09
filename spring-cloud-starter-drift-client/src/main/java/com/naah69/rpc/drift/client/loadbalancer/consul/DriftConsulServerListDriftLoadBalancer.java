package com.naah69.rpc.drift.client.loadbalancer.consul;

import com.google.common.collect.Lists;
import com.naah69.rpc.drift.client.common.AbstractDriftServerNodeListController;
import com.naah69.rpc.drift.client.common.DriftClientContext;
import com.naah69.rpc.drift.client.common.DriftServerNode;
import com.naah69.rpc.drift.client.discovery.IServerListUpdater;
import com.naah69.rpc.drift.client.discovery.consul.DriftConsulServerListUpdater;
import com.naah69.rpc.drift.client.discovery.consul.DriftConsulServerNode;
import com.naah69.rpc.drift.client.loadbalancer.AbstractDriftLoadBalancer;
import com.naah69.rpc.drift.client.loadbalancer.IRule;
import com.naah69.rpc.drift.client.properties.DriftClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * consul 服务列表负载均衡器
 *
 * @author naah
 */
public class DriftConsulServerListDriftLoadBalancer extends AbstractDriftLoadBalancer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriftConsulServerListDriftLoadBalancer.class);

    private AbstractDriftServerNodeListController serverNodeList;
    private IRule rule;

    private volatile IServerListUpdater serverListUpdater;

    private final com.naah69.rpc.drift.client.discovery.IServerListUpdater.IUpdateAction IUpdateAction = this::updateListOfServers;

    public DriftConsulServerListDriftLoadBalancer(AbstractDriftServerNodeListController serverNodeList, IRule rule) {
        DriftClientContext context = DriftClientContext.context();
        DriftClientProperties properties = context.getProperties();
        this.serverNodeList = serverNodeList;
        this.rule = rule;
        this.serverListUpdater = new DriftConsulServerListUpdater(properties.getRefreshInterval());
        this.startUpdateAction();
    }

    /**
     * 获取服务节点列表控制器
     *
     * @return
     */
    @Override
    public AbstractDriftServerNodeListController getThriftServerNodeList() {
        return this.serverNodeList;
    }

    /**
     * 选择服务节点
     *
     * @param key serviceName
     * @return
     */
    @Override
    public DriftConsulServerNode chooseServerNode(String key) {
        if (rule == null) {
            return null;
        } else {
            DriftServerNode serverNode;
            try {
                serverNode = rule.choose(key);
            } catch (Exception e) {
                LOGGER.warn("LoadBalancer [{}]:  Error choosing server for key {}", getClass().getSimpleName(), key, e);
                return null;
            }

            if (serverNode instanceof DriftConsulServerNode) {
                return (DriftConsulServerNode) serverNode;
            }
        }

        return null;
    }

    /**
     * 开启刷新节点任务
     */
    private synchronized void startUpdateAction() {
        LOGGER.info("Using serverListUpdater {}", serverListUpdater.getClass().getSimpleName());
        if (serverListUpdater == null) {
            serverListUpdater = new DriftConsulServerListUpdater();
        }

        this.serverListUpdater.start(IUpdateAction);
    }

    /**
     * 停止刷新节点任务
     */
    public void stopServerListRefreshing() {
        if (serverListUpdater != null) {
            serverListUpdater.stop();
        }
    }

    /**
     * 更新服务列表
     */
    private void updateListOfServers() {
        Map<String, LinkedHashSet<DriftConsulServerNode>> thriftConsulServers = this.serverNodeList.refreshThriftServers();

        List<String> serverList = Lists.newArrayList();
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, LinkedHashSet<DriftConsulServerNode>> serverEntry : thriftConsulServers.entrySet()) {
            serverList.add(
                    sb.append(serverEntry.getKey())
                            .append(": ")
                            .append(serverEntry.getValue())
                            .toString()
            );
            sb.setLength(0);
        }

        LOGGER.info("Refreshed drift serverList: [" + String.join(", ", serverList) + "]");
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DriftConsulServerListDriftLoadBalancer:");
        sb.append(super.toString());
        sb.append("ServerList:").append(String.valueOf(serverNodeList));
        return sb.toString();
    }

}
