package com.naah69.rpc.drift.client.pool;

import com.google.common.collect.Maps;
import com.naah69.rpc.drift.client.common.DriftServerNode;
import com.naah69.rpc.drift.client.exception.DriftClientConfigException;
import com.naah69.rpc.drift.client.properties.DriftClientPoolProperties;
import com.naah69.rpc.drift.client.properties.DriftClientProperties;
import com.naah69.rpc.drift.client.utils.DriftThreadCacheUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 连接池 对象工厂类
 * factory to create object in object pool
 *
 * @author naah
 */
public class DriftClientFactoryKeyedPooledObjectFactory extends BaseKeyedPooledObjectFactory<DriftServerNode, Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriftClientFactoryKeyedPooledObjectFactory.class);
    private static final AtomicInteger TRANSPORT_CONNECT_COUNT = new AtomicInteger();
    private static final AtomicInteger CLIENT_CONNECT_COUNT = new AtomicInteger();
    private DriftClientProperties properties;
    private final int MAX_PORT = 65535;
    private static final Map<Class, Map<DriftServerNode, io.airlift.drift.client.DriftClientFactory>> DRIFT_CLIENT_FACTORY_CACHE = Maps.newConcurrentMap();

    public DriftClientFactoryKeyedPooledObjectFactory(DriftClientProperties properties) {
        this.properties = properties;
    }


    /**
     * 创建连接
     * create connect
     *
     * @param key
     * @return
     * @throws Exception
     */
    @Override
    public Object create(DriftServerNode key) throws Exception {
        if (StringUtils.isBlank(key.getHost())) {
            throw new DriftClientConfigException("Invalid Thrift server, node IP address: " + key.getHost());
        }


        if (key.getPort() <= 0 || key.getPort() > MAX_PORT) {
            throw new DriftClientConfigException("Invalid Thrift server, node port: " + key.getPort());
        }
        Class clazz = DriftThreadCacheUtils.getClassThreadCache().get();

        Map<DriftServerNode, io.airlift.drift.client.DriftClientFactory> driftServerNodeDriftClientFactoryMap = DRIFT_CLIENT_FACTORY_CACHE.get(clazz);
        if (driftServerNodeDriftClientFactoryMap == null) {
            driftServerNodeDriftClientFactoryMap = Maps.newConcurrentMap();
            DRIFT_CLIENT_FACTORY_CACHE.put(clazz, driftServerNodeDriftClientFactoryMap);

        }
        io.airlift.drift.client.DriftClientFactory driftClientFactory = driftServerNodeDriftClientFactoryMap.get(key);

        if (driftClientFactory == null) {

            DriftClientPoolProperties poolProperties = properties.getPool();
            if (Objects.isNull(poolProperties)) {
                driftClientFactory = DriftClientFactory.determineTTranportAndProtocol(properties.getProtocolModel(), properties.getTransportModel(), key);

            } else {
                driftClientFactory = DriftClientFactory.determineTTranportAndProtocol(properties.getProtocolModel(), properties.getTransportModel(), key, poolProperties);

            }
            LOGGER.info("Established {}th socket transport, protocol is {}, transport is {}", TRANSPORT_CONNECT_COUNT.incrementAndGet(), properties.getProtocolModel(), properties.getTransportModel());
            driftServerNodeDriftClientFactoryMap.put(key, driftClientFactory);
        }
        LOGGER.debug("Create {}th socket client", CLIENT_CONNECT_COUNT.incrementAndGet());

        return driftClientFactory.createDriftClient(clazz).get();
    }


    /**
     * 包装连接
     * wrap connect
     *
     * @param value
     * @return
     */
    @Override
    public PooledObject<Object> wrap(Object value) {
        return new DefaultPooledObject<>(value);
    }


    /**
     * 验证连接
     * validate connect
     *
     * @param key
     * @param value
     * @return
     */
    @Override
    public boolean validateObject(DriftServerNode key, PooledObject<Object> value) {
        if (Objects.isNull(value)) {
            LOGGER.warn("PooledObject is already null");
            return false;
        }

        Object service = value.getObject();
        if (Objects.isNull(service)) {
            LOGGER.warn("Pooled transport is already null");
            return false;
        }

        try {
            return true;
        } catch (Exception e) {
            LOGGER.error(e.getCause().getMessage());
            return false;
        }
    }


    /**
     * 销毁连接
     * destroy connect
     *
     * @param key
     * @param value
     * @throws Exception
     */
    @Override
    public void destroyObject(DriftServerNode key, PooledObject<Object> value) throws Exception {
        if (Objects.nonNull(value)) {
            value.markAbandoned();
            LOGGER.debug("Destroy {}th socket client", CLIENT_CONNECT_COUNT.decrementAndGet());
        }
    }

}
