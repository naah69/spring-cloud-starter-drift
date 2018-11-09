package com.naah69.rpc.drift.client.loadbalancer;

import com.naah69.rpc.drift.client.common.DriftServerNode;

/**
 * 负载均衡规则接口
 *
 * @author naah
 */
public interface IRule {

    /**
     * 通过serviceName选择节点
     *
     * @param key
     * @return
     */
    DriftServerNode choose(String key);

    /**
     * 设置负载均衡器
     *
     * @param lb
     */
    void setLoadBalancer(ILoadBalancer lb);

    /**
     * 获取负载均衡器
     *
     * @return
     */
    ILoadBalancer getLoadBalancer();

}
