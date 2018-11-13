package com.naah69.rpc.drift.client.discovery.consul;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.catalog.CatalogClient;
import com.ecwid.consul.v1.health.HealthClient;
import com.ecwid.consul.v1.health.model.Check;
import com.ecwid.consul.v1.health.model.HealthService;
import com.ecwid.consul.v1.health.model.HealthService.Node;
import com.ecwid.consul.v1.health.model.HealthService.Service;
import com.naah69.rpc.drift.client.exception.DriftClientServerNodeListException;
import com.naah69.rpc.drift.client.utils.DriftReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Consul工具类
 * the utils of consul
 *
 * @author naah
 * @date 2018-11-07 7:04 PM
 * @desc
 */
public class DriftConsulServerUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriftConsulServerUtils.class);

    private static final String CHECK_STATUS_PASSING = "PASSING";

    private DriftConsulServerUtils() {
    }

    /**
     * 从健康节点获取主机地址
     * get host from health service
     *
     * @param healthservice
     * @return
     */
    public static String findHost(HealthService healthservice) {
        Service service = healthservice.getService();
        Node node = healthservice.getNode();
        if (StringUtils.hasText(service.getAddress())) {
            return fixIPv6Address(service.getAddress());
        } else {
            return StringUtils.hasText(node.getAddress()) ? fixIPv6Address(node.getAddress()) : node.getNode();
        }
    }

    /**
     * 转换IPv6地址
     * convert IPv6 adress
     *
     * @param address
     * @return
     */
    private static String fixIPv6Address(String address) {
        try {
            InetAddress inetAddress = InetAddress.getByName(address);
            return inetAddress instanceof Inet6Address ? "[" + inetAddress.getHostName() + "]" : address;
        } catch (UnknownHostException var2) {
            LOGGER.error("Not InetAddress: " + address + " , resolved as is.");
            return address;
        }
    }

    /**
     * 获取标签
     * get tags form health service
     *
     * @param healthservice
     * @return
     */
    public static List<String> getTags(HealthService healthservice) {
        return healthservice.getService().getTags();
    }


    /**
     * 测试是否健康
     * check service whatever is passing
     *
     * @param healthservice
     * @return
     */
    public static boolean isPassingCheck(HealthService healthservice) {
        List<Check> healthChecks = healthservice.getChecks();
        for (Check healthCheck : healthChecks) {
            if (!CHECK_STATUS_PASSING.equals(healthCheck.getStatus().name())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取元数据
     * get metadata from healthservice
     *
     * @param healthservice
     * @return
     */
    public static Map<String, String> getMetadata(HealthService healthservice) {
        return getMetadata(healthservice.getService().getTags());
    }

    /**
     * 获取元数据
     *
     * @param tags
     * @return
     */
    private static Map<String, String> getMetadata(List<String> tags) {
        LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
        if (tags != null) {

            for (String tag : tags) {
                String[] parts = StringUtils.delimitedListToStringArray(tag, "=");
                switch (parts.length) {
                    case 0:
                        break;
                    case 1:
                        metadata.put(parts[0], parts[0]);
                        break;
                    case 2:
                        metadata.put(parts[0], parts[1]);
                        break;
                    default:
                        String[] end = Arrays.copyOfRange(parts, 1, parts.length);
                        metadata.put(parts[0], StringUtils.arrayToDelimitedString(end, "="));
                }
            }
        }

        return metadata;
    }

    /**
     * 从Consul客户端获取CatalogClient
     * get catalog client from consul client
     *
     * @param consul
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public static CatalogClient getCatalogClient(ConsulClient consul) throws NoSuchFieldException, IllegalAccessException {
        RuntimeException e;
        Object catalogClientObj = DriftReflectionUtils.getObjByFieldFromTarget(ConsulField.CATALOG_CLIENT_FIELD_NAME, ConsulClient.class, consul);
        if (catalogClientObj instanceof CatalogClient && catalogClientObj != null) {
            return (CatalogClient) catalogClientObj;
        } else {
            String msg = "Try to get CatalogClient from ConsulClient failed";
            LOGGER.error(msg);
            e = new DriftClientServerNodeListException(msg);
            throw e;
        }
    }

    /**
     * 从Consul客户端获取Health client
     * get health client from consul client
     *
     * @param consul
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public static HealthClient getHealthClient(ConsulClient consul) throws NoSuchFieldException, IllegalAccessException {
        RuntimeException e;
        Object healthClientObj = DriftReflectionUtils.getObjByFieldFromTarget(ConsulField.HEALTH_CLIENT_FIELD_NAME, ConsulClient.class, consul);
        if (healthClientObj instanceof HealthClient && healthClientObj != null) {
            return (HealthClient) healthClientObj;
        } else {
            String msg = "Try to get HealthClient from ConsulClient failed";
            LOGGER.error(msg);
            e = new DriftClientServerNodeListException(msg);
            throw e;
        }
    }

}
