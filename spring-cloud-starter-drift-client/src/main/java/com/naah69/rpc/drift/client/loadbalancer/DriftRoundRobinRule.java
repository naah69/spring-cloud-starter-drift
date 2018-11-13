package com.naah69.rpc.drift.client.loadbalancer;

import com.naah69.rpc.drift.client.common.DriftServerNode;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 负载均衡轮训规则
 * load balance rule of robin
 *
 * @author naah
 */
public class DriftRoundRobinRule extends AbstractDriftLoadBalancerRule {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriftRoundRobinRule.class);
    public static final int SERVERNODE_CHOOSE_TIMES = 10;
    private AtomicInteger nextServerCyclicCounter;

    public DriftRoundRobinRule() {
        nextServerCyclicCounter = new AtomicInteger();
    }

    public DriftRoundRobinRule(ILoadBalancer lb) {
        this();
        setLoadBalancer(lb);
    }

    /**
     * 通过serviceName选择服务节点
     * choose server name by service name
     *
     * @param key
     * @return
     */
    @Override
    public DriftServerNode choose(String key) {
        return choose(getLoadBalancer(), key);
    }

    /**
     * 通过负载均衡器和serviceName选择服务节点
     * chooser server name by load balance and service name
     *
     * @param lb  负载均衡器
     * @param key serviceName
     * @return
     */
    @SuppressWarnings("unchecked")
    private DriftServerNode choose(ILoadBalancer lb, String key) {
        if (lb == null) {
            LOGGER.warn("No specified load balancer");
            return null;
        }

        List<DriftServerNode> serverNodes;
        DriftServerNode serverNode = null;
        int count = 0;

        /**
         * 获取10次
         * try getting 10 times
         */
        while (serverNode == null && count++ < SERVERNODE_CHOOSE_TIMES) {
            /**
             * 获取列表
             * get server node list
             */
            serverNodes = lb.getServerNodes(key);

            if (CollectionUtils.isEmpty(serverNodes)) {
                List refreshedServerNodes = lb.getRefreshedServerNodes(key);


                if (CollectionUtils.isEmpty(refreshedServerNodes)) {
                    LOGGER.warn("No up servers of key {}, available from load balancer: " + lb, key);
                    return null;
                }

                serverNodes = refreshedServerNodes;
            }

            /**
             * 索引增加一个
             * index increment
             */
            int nextServerIndex = incrementAndGetModulo(serverNodes.size());

            /**
             * 获取节点
             * get node
             */
            serverNode = serverNodes.get(nextServerIndex);

            if (serverNode == null) {
                Thread.yield();
            }
        }

        if (count >= SERVERNODE_CHOOSE_TIMES) {
            LOGGER.warn("No available alive server nodes after 10 tries from load balancer: "
                    + lb);
        }

        return serverNode;
    }

    /**
     * 增加并且获取一个
     * increment and get index
     *
     * @param modulo
     * @return
     */
    private int incrementAndGetModulo(int modulo) {
        for (; ; ) {
            int current = nextServerCyclicCounter.get();
            int next = (current + 1) % modulo;
            if (nextServerCyclicCounter.compareAndSet(current, next)) {
                return next;
            }
        }
    }

}
