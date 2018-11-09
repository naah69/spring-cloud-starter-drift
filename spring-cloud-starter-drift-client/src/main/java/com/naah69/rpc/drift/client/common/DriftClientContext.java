package com.naah69.rpc.drift.client.common;

import com.naah69.rpc.drift.client.pool.DriftServiceKeyedObjectPool;
import com.naah69.rpc.drift.client.properties.DriftClientProperties;
import com.naah69.rpc.drift.client.utils.DriftRegistryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.composite.CompositeDiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thrift客户端上下文
 * 单例
 *
 * @author naah
 */
public class DriftClientContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriftClientContext.class);

    private static final Lock LOCK = new ReentrantLock();

    private static DriftClientContext context;

    private DriftClientContext() {
    }

    /**
     * 客户端配置文件
     */
    private DriftClientProperties properties;

    /**
     * 对象池
     */
    private DriftServiceKeyedObjectPool objectPool;

    /**
     * 注册中心名称
     */
    private String registryName;

    /**
     * 注册地址
     */
    private String registryAddress;

    /**
     * SpringCloud Discory客户端
     */
    private DiscoveryClient client;

    /**
     * 真实的注册中心客户端
     */
    private Object registryClient;

    /**
     * 环境
     */
    private Environment env;

    /**
     * Spring Contetx
     */
    private ApplicationContext applicationContext;


    public static DriftClientContext context(DriftClientProperties properties, DriftServiceKeyedObjectPool objectPool) {
        DriftClientContext context = context();
        context.properties = properties;
        context.objectPool = objectPool;
        return context;
    }

    public static DriftClientContext context() {
        if (context == null) {
            try {
                LOCK.lock();
                if (context == null) {
                    LOGGER.info("init DriftClientContext");
                    context = new DriftClientContext();
                }
            } finally {
                LOCK.unlock();
            }
        }
        return context;
    }

    public static DriftClientContext context(ApplicationContext applicationContext) throws NoSuchFieldException, IllegalAccessException {
        DriftClientContext context = context();
        context.applicationContext = applicationContext;
        if (context.client == null || context.env == null) {
            context.registry(applicationContext.getBean(DiscoveryClient.class), applicationContext.getEnvironment());
        }
        return context;
    }

    /**
     * 注册
     *
     * @param client      SpringCloud Discovery客户端
     * @param environment Spring 环境
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private void registry(DiscoveryClient client, Environment environment) throws NoSuchFieldException, IllegalAccessException {
        LOGGER.debug("load registry information into DriftClientContext");
        context().env = environment;
        client = getRealDiscoveryClient(client);
        context().client = client;
        context().registryName = client.description();
        context().registryAddress = DriftRegistryUtils.getRegistryAddress();
        context().registryClient = DriftRegistryUtils.getRegistryClient();


    }

    /**
     * 获取包含真实Client的Discovery客户端
     * <p>
     * 因为Spring Cloud 2.0.1以后加入一层封装
     * 默认使用{@link CompositeDiscoveryClient}
     *
     * @param client SpringCloud Discovery客户端
     * @return Discovery客户端
     */
    private DiscoveryClient getRealDiscoveryClient(DiscoveryClient client) {
        if (client instanceof CompositeDiscoveryClient) {
            CompositeDiscoveryClient compositeDiscoveryClient = (CompositeDiscoveryClient) client;
            List<DiscoveryClient> discoveryClientList = compositeDiscoveryClient.getDiscoveryClients();
            for (DiscoveryClient discoveryClient : discoveryClientList) {
                if (!(discoveryClient instanceof CompositeDiscoveryClient)) {
                    client = discoveryClient;
                    break;
                }
            }
        }
        return client;
    }

    public DriftClientProperties getProperties() {
        if (context.properties == null) {
            context.properties = context.applicationContext.getBean(DriftClientProperties.class);
        }
        return context.properties;
    }

    public DriftServiceKeyedObjectPool getObjectPool() {
        return context.objectPool;
    }

    public String getRegistryAddress() {
        return context.registryAddress;
    }

    public String getRegistryName() {
        return registryName;
    }

    public DiscoveryClient getClient() {
        return client;
    }

    public void setClient(DiscoveryClient client) {
        this.client = client;
    }

    public Environment getEnv() {
        return env;
    }

    public Object getRegistryClient() {
        return registryClient;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
