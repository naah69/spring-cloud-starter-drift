package com.naah69.rpc.drift.client.scanner;


import com.naah69.rpc.drift.client.annotation.ThriftRefer;
import com.naah69.rpc.drift.client.cache.DriftServiceMethodCacheManager;
import com.naah69.rpc.drift.client.common.AbstractDriftServerNodeListController;
import com.naah69.rpc.drift.client.common.DriftClientContext;
import com.naah69.rpc.drift.client.common.DriftServerNode;
import com.naah69.rpc.drift.client.common.DriftServiceSignature;
import com.naah69.rpc.drift.client.exception.DriftClientException;
import com.naah69.rpc.drift.client.exception.DriftClientRegistryException;
import com.naah69.rpc.drift.client.loadbalancer.DriftLoadBalancerFactory;
import com.naah69.rpc.drift.client.loadbalancer.DriftRoundRobinRule;
import com.naah69.rpc.drift.client.loadbalancer.ILoadBalancer;
import com.naah69.rpc.drift.client.loadbalancer.IRule;
import com.naah69.rpc.drift.client.pool.DriftServiceKeyedObjectPool;
import com.naah69.rpc.drift.client.properties.DriftClientPoolProperties;
import com.naah69.rpc.drift.client.properties.DriftClientProperties;
import com.naah69.rpc.drift.client.utils.DriftAnnotationUtils;
import com.naah69.rpc.drift.client.utils.DriftRegistryUtils;
import com.naah69.rpc.drift.client.utils.DriftThreadCacheUtils;
import io.airlift.drift.client.UncheckedTTransportException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 截面
 * 远程调用 方法拦截 来负载均衡一个服务节点
 *
 * @author naah
 */
public class DriftClientAdvice implements MethodInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriftClientAdvice.class);

    private DriftServiceSignature serviceSignature;


    private ILoadBalancer loadBalancer;

    private DriftClientProperties properties;

    private DriftServiceKeyedObjectPool objectPool;

    public DriftClientAdvice(DriftServiceSignature serviceSignature
    ) {
        this.serviceSignature = serviceSignature;

        DriftClientContext context = DriftClientContext.context();
        DiscoveryClient client = context.getClient();
        String registryName = context.getRegistryName();
        String registerAddress = context.getRegistryAddress();

        if (Objects.isNull(client)) {
            DriftClientRegistryException exception = new DriftClientRegistryException("Unable to access register");
            LOGGER.error("Unable to access register, address is: {}, type is: {}", registerAddress, registryName);
            LOGGER.error("", exception);
            throw exception;
        }

        AbstractDriftServerNodeListController serverNodeListController = DriftRegistryUtils.getThriftServerNodeListController();

        IRule routerRule = new DriftRoundRobinRule();
        this.loadBalancer = DriftLoadBalancerFactory.getLoadBalancer(serverNodeListController, routerRule);

        /**
         *设置负载均衡
         */
        routerRule.setLoadBalancer(loadBalancer);
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {


        DriftClientContext context = DriftClientContext.context();


        /**
         *获取配置文件
         */
        if (Objects.isNull(properties)) {
            this.properties = context.getProperties();
        }

        /**
         *获取对象池
         */
        if (Objects.isNull(objectPool)) {
            this.objectPool = context.getObjectPool();
        }

        /**
         *获取要调用的方法
         */
        Method invocationMethod = invocation.getMethod();
        Object[] args = invocation.getArguments();
        Class<?> declaringClass = invocationMethod.getDeclaringClass();

        /**
         * 获取调用方的注解
         */
        ThriftRefer thriftReferAnnotation = DriftAnnotationUtils.getThriftReferAnnotation(invocationMethod);
        String version = thriftReferAnnotation.version();

        /**
         *获取对象池配置
         */
        DriftClientPoolProperties poolProperties = properties.getPool();

        /**
         *从签名获取serviceId
         */
        String serviceId = serviceSignature.getThriftServiceId();

        String serviceName = StringUtils.isNotBlank(version) ? serviceId + "$" + version : serviceId;
        /**
         *负载均衡选择一个节点
         */
        DriftServerNode serverNode = loadBalancer.chooseServerNode(serviceName);
        if (serverNode == null) {
            LOGGER.warn("cant't find serverNode. serviceId:{},version:{}", serviceId, version);
        }
        /**
         *生成服务签名信息
         */
        String signature = serviceSignature.marker();


        /**
         *重试次数
         */
        int retryTimes = 0;
        Object service = null;
        while (true) {

            /**
             *如果超过重试次数
             */
            if (retryTimes++ > poolProperties.getRetryTimes()) {
                LOGGER.error(
                        "All drift client call failed, method is {}, args is {}, retryTimes: {}",
                        invocation.getMethod().getName(), args, retryTimes);
                throw new DriftClientException("Thrift client call failed, drift client signature is: " + signature);
            }
            try {

                /**
                 *从对象池取出节点
                 */
                DriftThreadCacheUtils.getClassThreadCache().set(declaringClass);
                service = objectPool.borrowObject(serverNode);

                /**
                 *获取缓存的方法
                 */
                Method cachedMethod = DriftServiceMethodCacheManager.getMethod(service.getClass(),
                        invocationMethod.getName(),
                        invocationMethod.getParameterTypes());

                /**
                 *返回调用结果
                 */
                return ReflectionUtils.invokeMethod(cachedMethod, service, args);

            } catch (IllegalArgumentException | IllegalAccessException | InstantiationException | SecurityException | NoSuchMethodException e) {
                LOGGER.error(" Unable to open drift client", e);
            } catch (UncheckedTTransportException e) {
                LOGGER.error(" rpc access failed for time out exception", e);
            } catch (Exception e) {
                LOGGER.error(" rpc access failed for exception", e);

            } finally {
                //清空
                DriftThreadCacheUtils.getClassThreadCache().set(null);
                try {
                    if (objectPool != null && service != null) {
                        objectPool.returnObject(serverNode, service);
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }

    }

}
