package com.naah69.rpc.drift.client.discovery.consul;

import com.ecwid.consul.v1.health.model.Check;
import com.ecwid.consul.v1.health.model.HealthService;
import com.ecwid.consul.v1.health.model.HealthService.Node;
import com.ecwid.consul.v1.health.model.HealthService.Service;
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
 * @author naah
 */
public class DriftConsulServerUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriftConsulServerUtils.class);

    private static final String CHECK_STATUS_PASSING = "PASSING";

    private DriftConsulServerUtils() {
    }

    /**
     * 转换地址
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

    public static List<String> getTags(HealthService healthservice) {
        return healthservice.getService().getTags();
    }


    /**
     * 测试是否健康
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

}
