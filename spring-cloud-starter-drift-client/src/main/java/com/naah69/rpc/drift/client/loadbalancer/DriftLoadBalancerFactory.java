package com.naah69.rpc.drift.client.loadbalancer;

import com.naah69.rpc.drift.client.common.AbstractDriftServerNodeListController;
import com.naah69.rpc.drift.client.common.DriftClientContext;
import com.naah69.rpc.drift.client.common.DriftServerNode;
import com.naah69.rpc.drift.client.discovery.RegistryEnum;
import com.naah69.rpc.drift.client.exception.DriftClientServerNodeListException;
import com.naah69.rpc.drift.client.loadbalancer.consul.DriftConsulServerListDriftLoadBalancer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DriftLoadBalancerFactory
 *
 * @author naah
 * @date 2018-11-03 8:05 PM
 * @desc
 */
public class DriftLoadBalancerFactory {
    private static Logger LOGGER = LoggerFactory.getLogger(DriftLoadBalancerFactory.class);
    private static DriftClientContext context;

    /**
     * 获取服务节点负载均衡器
     *
     * @param serverNodeListController
     * @param routerRule
     * @return
     */
    public static ILoadBalancer<DriftServerNode> getLoadBalancer(AbstractDriftServerNodeListController serverNodeListController, IRule routerRule) {
        if (context == null) {
            context = DriftClientContext.context();
        }

        RegistryEnum registryDescription = RegistryEnum.valueOf(context.getRegistryName().replace(" ", ""));
        switch (registryDescription) {
            case SpringCloudConsulDiscoveryClient:
                return new DriftConsulServerListDriftLoadBalancer(serverNodeListController, routerRule);
            default:
                LOGGER.error("try to get getLoadBalancer failed. registryDescription:{}", registryDescription);
                throw new DriftClientServerNodeListException("ry to get getLoadBalancer failed.");
        }

    }
}
