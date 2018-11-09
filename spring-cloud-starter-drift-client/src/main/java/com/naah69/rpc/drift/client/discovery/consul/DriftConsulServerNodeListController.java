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
 * Consul服务节点列表（单例）
 *
 * @author naah
 */

public class DriftConsulServerNodeListController extends AbstractDriftServerNodeListController<DriftConsulServerNode> {

    public static final String SERVICE_NAME_SPLIT = "$";
    private static Logger LOGGER = LoggerFactory.getLogger(DriftConsulServerNodeListController.class);
    private ConsulClient consul;
    private HealthClient healthClient;
    private CatalogClient catalogClient;

    private static DriftConsulServerNodeListController serverNodeListController = null;


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
            /**
             *反射获取ConsulClient
             */
            consul = client;

            /**
             *反射获取healthClient
             */
            healthClient = ConsulUtils.getHealthClient(consul);

            /**
             *反射获取catalogClient
             */
            catalogClient = ConsulUtils.getCatalogClient(consul);

        } catch (NoSuchFieldException e) {
            String msg = "try to get ConsulClient from DiscoveryClient failed! but can't find field";
            LOGGER.error(msg, e);
            throw new DriftClientServerNodeListException(msg);
        } catch (IllegalAccessException e) {
            String msg = "try to get ConsulClient from DiscoveryClient failed! the field refused to access";
            LOGGER.error(msg, e);
            throw new DriftClientServerNodeListException(msg);
        }

    }

    /**
     * 通过serviceName获取节点列表
     *
     * @param serviceName 服务名 serviceId$version 或 serviceId
     * @return
     */
    @Override
    public List<DriftConsulServerNode> getThriftServer(String serviceName) {

        /**
         * 分解serverId和version
         */
        String version = null;
        if (StringUtils.isNotBlank(serviceName) && serviceName.contains(SERVICE_NAME_SPLIT)) {
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
                         */
                        serverNodes.add(driftNode);
                    } else if (StringUtils.isBlank(version) && (driftNode.getTags() == null || driftNode.getTags().size() == 0)) {
                        /**
                         * 没有版本号
                         */
                        serverNodes.add(driftNode);
                    }
                }

                return serverNodes;

            }
        }

        /**
         *根据服务名刷新服务列表
         */
        return refreshThriftServer(serviceName);
    }

    /**
     * 通过serviceName刷新节点列表
     *
     * @param serviceName 服务名 serviceId$version 或 serviceId
     * @return
     */
    @Override
    public List<DriftConsulServerNode> refreshThriftServer(String serviceName) {
        /**
         * 分解serverId和version
         */
        String version = null;
        if (StringUtils.isNotBlank(serviceName) && serviceName.contains(SERVICE_NAME_SPLIT)) {
            String[] split = serviceName.split("\\$");
            serviceName = split[0];
            version = split[1];
        }

        List<DriftConsulServerNode> serverNodeList = Lists.newLinkedList();

        /**
         * 获取健康节点
         */
        List<HealthService> healthservicelist = healthClient.getHealthServices(serviceName, true, QueryParams.DEFAULT).getValue();

        /**
         *过滤节点
         */
        filterAndCompoServerNodes(version, serverNodeList, healthservicelist);

        if (CollectionUtils.isNotEmpty(serverNodeList)) {
            /**
             * 加入缓存
             */
            this.serverNodeMap.put(serviceName, Sets.newLinkedHashSet(serverNodeList));
        }

        return serverNodeList;
    }

    /**
     * 获取所有服务节点列表
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
     * 刷新所有节点服务列表
     *
     * @return
     */
    @Override
    public Map<String, LinkedHashSet<DriftConsulServerNode>> refreshThriftServers() {
        /**
         * 获取consul节点目录
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
