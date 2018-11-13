package com.naah69.rpc.drift.client.loadbalancer;

import com.naah69.rpc.drift.client.common.DriftServerNode;

/**
 * 负载均衡规则接口
 * interface of load balancer rule
 *
 * @author naah
 */
public interface IRule {

    /**
     * 通过serviceName选择节点
     * chooser server node by service name
     *
     * @param key
     * @return
     */
    DriftServerNode choose(String key);

    /**
     * 设置负载均衡器
     * set load balancer
     *
     * @param lb
     */
    void setLoadBalancer(ILoadBalancer lb);

    /**
     * 获取负载均衡器
     * get load balancer
     *
     * @return
     */
    ILoadBalancer getLoadBalancer();

}
