package com.naah69.rpc.drift.client.discovery.consul;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.catalog.CatalogClient;
import com.ecwid.consul.v1.health.HealthClient;
import com.ecwid.consul.v1.health.model.HealthService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.naah69.rpc.drift.client.common.AbstractDriftServerNodeListController;
import com.naah69.rpc.drift.client.exception.DriftClientServerNodeListException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Consul服务节点列表控制器（单例）
 * controller of consul server node list. Singleton pattern
 *
 * @author naah
 */

public class DriftConsulServerNodeListController extends AbstractDriftServerNodeListController<DriftConsulServerNode> {

    private static Logger LOGGER = LoggerFactory.getLogger(DriftConsulServerNodeListController.class);

    private ConsulClient consul;
    private HealthClient healthClient;
    private CatalogClient catalogClient;

    private static DriftConsulServerNodeListController serverNodeListController = null;

    /**
     * 单例模式创建对象
     * Singleton pattern to create Object
     *
     * @param client
     * @return
     */
    public static AbstractDriftServerNodeListController singleton(ConsulClient client) {
        if (serverNodeListController == null) {
            synchronized (DriftConsulServerNodeListController.class) {
                if (serverNodeListController == null) {
                    serverNodeListController = new DriftConsulServerNodeListController(client);
                }
            }
        }
        return serverNodeListController;
    }

    private DriftConsulServerNodeListController(ConsulClient client) {

        try {
            consul = client;

            /**
             * 反射获取healthClient
             * use reflection to get healthClient
             */
            healthClient = DriftConsulServerUtils.getHealthClient(consul);

            /**
             * 反射获取catalogClient
             * use reflection to get catalogClient
             */
            catalogClient = DriftConsulServerUtils.getCatalogClient(consul);

        } catch (NoSuchFieldException e) {
            String msg = "Try to get ConsulClient from DiscoveryClient failed! But can't find field";
            LOGGER.error(msg, e);
            throw new DriftClientServerNodeListException(msg);
        } catch (IllegalAccessException e) {
            String msg = "Try to get ConsulClient from DiscoveryClient failed! The field refused to access";
            LOGGER.error(msg, e);
            throw new DriftClientServerNodeListException(msg);
        }

    }

    /**
     * 通过serviceName获取节点列表
     * get node list by service name
     *
     * @param serviceName format serviceId$version or serviceId
     * @return
     */
    @Override
    public List<DriftConsulServerNode> getThriftServer(String serviceName) {

        /**
         * 分解serverId和version
         * get serverId and version from serviceName
         */
        String version = null;
        if (StringUtils.isNotBlank(serviceName) && serviceName.contains(ConsulField.SERVICE_NAME_SPLIT)) {
            String[] split = serviceName.split("\\$");
            serviceName = split[0];
            version = split[1];
        }

        if (MapUtils.isNotEmpty(this.serverNodeMap) && (this.serverNodeMap.containsKey(serviceName))) {

            LinkedHashSet<DriftConsulServerNode> serverNodeSet = this.serverNodeMap.get(serviceName);

            if (CollectionUtils.isNotEmpty(serverNodeSet)) {
                List<DriftConsulServerNode> serverNodes = Lists.newLinkedList();
                for (DriftConsulServerNode driftNode : serverNodeSet) {

                    if (StringUtils.isNotBlank(version) && driftNode.getTags().contains(version)) {
                        /**
                         * 有版本号
                         * if it has version
                         */
                        serverNodes.add(driftNode);
                    } else if (StringUtils.isBlank(version) && (driftNode.getTags() == null || driftNode.getTags().size() == 0)) {
                        /**
                         * 没有版本号
                         * no version
                         */
                        serverNodes.add(driftNode);
                    }
                }

                return serverNodes;

            }
        }

        /**
         * 根据服务名刷新服务列表
         * refresh server list by service name
         */
        return refreshThriftServer(serviceName);
    }

    /**
     * 通过serviceName刷新节点列表
     * refresh server list by service name
     *
     * @param serviceName format serviceId$version or serviceId
     * @return
     */
    @Override
    public List<DriftConsulServerNode> refreshThriftServer(String serviceName) {
        /**
         * 分解serverId和version
         * get serverId and version from serviceName
         */
        String version = null;
        if (StringUtils.isNotBlank(serviceName) && serviceName.contains(ConsulField.SERVICE_NAME_SPLIT)) {
            String[] split = serviceName.split("\\$");
            serviceName = split[0];
            version = split[1];
        }

        List<DriftConsulServerNode> serverNodeList = Lists.newLinkedList();

        /**
         * get health node
         */
        List<HealthService> healthservicelist = healthClient.getHealthServices(serviceName, true, QueryParams.DEFAULT).getValue();

        /**
         * filter node
         */
        filterAndCompoServerNodes(version, serverNodeList, healthservicelist);

        if (CollectionUtils.isNotEmpty(serverNodeList)) {
            /**
             * put them into server node map
             */
            this.serverNodeMap.put(serviceName, Sets.newLinkedHashSet(serverNodeList));
        }

        return serverNodeList;
    }

    /**
     * get all thrift server node map
     *
     * @return
     */
    @Override
    public Map<String, LinkedHashSet<DriftConsulServerNode>> getThriftServers() {
        if (MapUtils.isNotEmpty(this.serverNodeMap)) {
            return this.serverNodeMap;
        }

        return refreshThriftServers();
    }

    /**
     * refresh all thrift server node map
     *
     * @return
     */
    @Override
    public Map<String, LinkedHashSet<DriftConsulServerNode>> refreshThriftServers() {
        /**
         * get catalog
         */
        Map<String, List<String>> catalogServiceMap = catalogClient.getCatalogServices(QueryParams.DEFAULT).getValue();
        if (MapUtils.isEmpty(catalogServiceMap)) {
            return this.serverNodeMap;
        }

        Map<String, LinkedHashSet<DriftConsulServerNode>> serverNodeMap = Maps.newConcurrentMap();
        for (Map.Entry<String, List<String>> catalogServiceEntry : catalogServiceMap.entrySet()) {
            String serviceName = catalogServiceEntry.getKey();

            List<HealthService> healthservicelist = healthClient.getHealthServices(serviceName, true, QueryParams.DEFAULT).getValue();

            LinkedHashSet<DriftConsulServerNode> serverNodeSet = Sets.newLinkedHashSet();

            List<DriftConsulServerNode> serverNodes = Lists.newArrayList();

            filterAndCompoServerNodes(null, serverNodes, healthservicelist);

            serverNodeSet.addAll(serverNodes);
            if (CollectionUtils.isNotEmpty(serverNodeSet)) {
                serverNodeMap.put(serviceName, serverNodeSet);
                this.serverNodeMap.putAll(serverNodeMap);
            }
        }

        return this.serverNodeMap;
    }


    /**
     * Consul的HealthService节点 转化为 DriftConsulServerNode
     * convert health service node to drift consul server node
     *
     * @param healthservice
     * @return
     */
    private static DriftConsulServerNode getThriftConsulServerNode(HealthService healthservice) {
        DriftConsulServerNode serverNode = new DriftConsulServerNode();

        HealthService.Node node = healthservice.getNode();
        serverNode.setNode(node.getNode());

        HealthService.Service service = healthservice.getService();
        serverNode.setAddress(service.getAddress());
        serverNode.setPort(service.getPort());
        serverNode.setHost(DriftConsulServerUtils.findHost(healthservice));

        serverNode.setServiceId(service.getService());
        serverNode.setTags(service.getTags());
        serverNode.setHealth(DriftConsulServerUtils.isPassingCheck(healthservice));

        return serverNode;
    }


    /**
     * 过滤服务节点
     * filter service node
     *
     * @param version
     * @param serverNodeList
     * @param healthservicelist
     */
    private void filterAndCompoServerNodes(String version, List<DriftConsulServerNode> serverNodeList, List<HealthService> healthservicelist) {
        for (HealthService healthservice : healthservicelist) {
            DriftConsulServerNode serverNode = getThriftConsulServerNode(healthservice);

            if (serverNode == null) {
                continue;
            }

            if (!serverNode.isHealth()) {
                continue;
            }

            if (StringUtils.isNotBlank(version)) {

                if (!serverNode.getTags().contains(version)) {
                    continue;
                }

            } else if (StringUtils.isBlank(version) && (serverNode.getTags() != null || serverNode.getTags().size() != 0)) {
                continue;
            }
            serverNodeList.add(serverNode);
        }
    }


}
