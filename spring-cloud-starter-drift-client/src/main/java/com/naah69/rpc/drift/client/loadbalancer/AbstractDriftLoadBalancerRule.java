package com.naah69.rpc.drift.client.loadbalancer;

/**
 * 负载均衡规则抽象类
 *
 * @author naah
 */
public abstract class AbstractDriftLoadBalancerRule implements IRule {

    protected ILoadBalancer lb;

    @Override
    public void setLoadBalancer(ILoadBalancer lb) {
        this.lb = lb;
    }

    @Override
    public ILoadBalancer getLoadBalancer() {
        return lb;
    }

}
