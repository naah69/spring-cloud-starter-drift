package com.naah69.rpc.drift.client.utils;

import com.ecwid.consul.v1.ConsulClient;
import com.naah69.rpc.drift.client.common.AbstractDriftServerNodeListController;
import com.naah69.rpc.drift.client.common.DriftClientContext;
import com.naah69.rpc.drift.client.discovery.RegistryEnum;
import com.naah69.rpc.drift.client.discovery.consul.ConsulField;
import com.naah69.rpc.drift.client.discovery.consul.DriftConsulServerNodeListController;
import com.naah69.rpc.drift.client.exception.DriftClientRegistryException;
import com.naah69.rpc.drift.client.exception.DriftClientServerNodeListException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryClient;
import org.springframework.core.env.Environment;

/**
 * DriftRegistryUtils
 *
 * @author naah
 * @date 2018-11-03 2:28 PM
 * @desc
 */
public class DriftRegistryUtils {
    private static Logger LOGGER = LoggerFactory.getLogger(DriftRegistryUtils.class);

    private static DriftClientContext context;

    private static String host;
    private static int port = -1;

    /**
     * 获取注册中心地址
     *
     * @return
     */
    public static String getRegistryAddress() {
        if (StringUtils.isBlank(host)) {
            getHost();
        }
        if (port == -1) {
            getPort();
        }

        if (StringUtils.isBlank(host) && port != -1) {
            DriftClientRegistryException exception = new DriftClientRegistryException("can't find infomation of registry");
            LOGGER.error("can't find infomation of registry. host:{} , port:{}", host, port);
            LOGGER.error("", exception);
            throw exception;
        }
        return String.format(ConsulField.ADDRESS_TEMPLATE, host, port);
    }

    /**
     * 获取主机地址
     *
     * @return
     */
    public static String getHost() {
        if (context == null) {
            context = DriftClientContext.context();
        }
        if (StringUtils.isBlank(host)) {
            RegistryEnum registryDescription = RegistryEnum.valueOf(context.getRegistryName().replace(" ", ""));
            Environment env = context.getEnv();
            switch (registryDescription) {
                case SpringCloudConsulDiscoveryClient:
                    host = env.getProperty(ConsulField.SPRING_CLOUD_CONSUL_HOST);
                    break;
                default:
                    DriftClientRegistryException exception = new DriftClientRegistryException("get host failed ,cause: can't find right registry kind ");
                    LOGGER.error("get host failed ,cause: can't find right registry kind . registryDescription:{} ", registryDescription);
                    LOGGER.error("", exception);
                    throw exception;
            }

        }
        return host;

    }

    /**
     * 获取端口
     *
     * @return
     */
    public static int getPort() {
        if (context == null) {
            context = DriftClientContext.context();
        }
        if (port == -1) {
            RegistryEnum registryDescription = RegistryEnum.valueOf(context.getRegistryName().replace(" ", ""));
            Environment env = context.getEnv();
            switch (registryDescription) {
                case SpringCloudConsulDiscoveryClient:
                    port = env.getProperty(ConsulField.SPRING_CLOUD_CONSUL_PORT, int.class);
                    break;
                default:
                    DriftClientRegistryException exception = new DriftClientRegistryException("get port failed ,cause: can't find right registry kind ");
                    LOGGER.error("get port failed ,cause: can't find right registry kind . registryDescription:{} ", registryDescription);
                    LOGGER.error("", exception);
                    throw exception;
            }

        }
        return port;

    }

    /**
     * 获取服务节点列表控制器
     *
     * @return
     */
    public static AbstractDriftServerNodeListController getThriftServerNodeListController() {
        if (context == null) {
            context = DriftClientContext.context();
        }
        RegistryEnum registryDescription = RegistryEnum.valueOf(context.getRegistryName().replace(" ", ""));
        Object registryClient = context.getRegistryClient();
        switch (registryDescription) {
            case SpringCloudConsulDiscoveryClient:
                ConsulClient consul = (ConsulClient) registryClient;
                return DriftConsulServerNodeListController.singleton(consul);
            default:
                LOGGER.error("try to get AbstractDriftServerNodeListController failed. registryDescription:{}", registryDescription);
                throw new DriftClientServerNodeListException("try to get AbstractDriftServerNodeListController failed.");
        }
    }

    /**
     * 获取注册中心客户端
     *
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public static Object getRegistryClient() throws NoSuchFieldException, IllegalAccessException {
        if (context == null) {
            context = DriftClientContext.context();
        }
        RegistryEnum registryDescription = RegistryEnum.valueOf(context.getRegistryName().replace(" ", ""));
        String host = DriftRegistryUtils.getHost();
        switch (registryDescription) {
            case SpringCloudConsulDiscoveryClient:
                DiscoveryClient client = context.getClient();
                return DriftReflectionUtils.getObjByFieldFromTarget("client", ConsulDiscoveryClient.class, client);
            default:
                LOGGER.error("try to get RegistryClient failed. registryDescription:{}, host:{}", registryDescription, host);
                throw new DriftClientServerNodeListException("try to get AbstractDriftServerNodeListController failed.");
        }

    }


}
