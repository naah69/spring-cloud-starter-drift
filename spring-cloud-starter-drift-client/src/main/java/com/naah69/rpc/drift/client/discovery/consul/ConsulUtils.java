package com.naah69.rpc.drift.client.discovery.consul;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.catalog.CatalogClient;
import com.ecwid.consul.v1.health.HealthClient;
import com.naah69.rpc.drift.client.exception.DriftClientServerNodeListException;
import com.naah69.rpc.drift.client.utils.DriftReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConsulUtils
 *
 * @author naah
 * @date 2018-11-07 7:04 PM
 * @desc
 */
public class ConsulUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsulUtils.class);

    public static CatalogClient getCatalogClient(ConsulClient consul) throws NoSuchFieldException, IllegalAccessException {
        RuntimeException e;
        Object catalogClientObj = DriftReflectionUtils.getObjByFieldFromTarget("catalogClient", ConsulClient.class, consul);
        if (catalogClientObj instanceof CatalogClient && catalogClientObj != null) {
            return (CatalogClient) catalogClientObj;
        } else {
            String msg = "try to get CatalogClient from ConsulClient failed";
            LOGGER.error(msg);
            e = new DriftClientServerNodeListException(msg);
            throw e;
        }
    }

    public static HealthClient getHealthClient(ConsulClient consul) throws NoSuchFieldException, IllegalAccessException {
        RuntimeException e;
        Object healthClientObj = DriftReflectionUtils.getObjByFieldFromTarget("healthClient", ConsulClient.class, consul);
        if (healthClientObj instanceof HealthClient && healthClientObj != null) {
            return (HealthClient) healthClientObj;
        } else {
            String msg = "try to get HealthClient from ConsulClient failed";
            LOGGER.error(msg);
            e = new DriftClientServerNodeListException(msg);
            throw e;
        }
    }
}
