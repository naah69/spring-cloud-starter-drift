package com.naah69.rpc.drift.client.pool;

import com.google.common.collect.ImmutableList;
import com.google.common.net.HostAndPort;
import com.naah69.rpc.drift.client.common.DriftServerNode;
import com.naah69.rpc.drift.client.exception.DriftClientConfigException;
import com.naah69.rpc.drift.client.properties.DriftClientPoolProperties;
import com.naah69.rpc.drift.client.properties.DriftProtocolModel;
import com.naah69.rpc.drift.client.properties.DriftTransportModel;
import io.airlift.drift.client.ExceptionClassifier;
import io.airlift.drift.client.address.SimpleAddressSelector;
import io.airlift.drift.codec.ThriftCodecManager;
import io.airlift.drift.transport.netty.client.DriftNettyClientConfig;
import io.airlift.drift.transport.netty.client.DriftNettyMethodInvokerFactory;
import io.airlift.drift.transport.netty.codec.Protocol;
import io.airlift.drift.transport.netty.codec.Transport;
import io.airlift.units.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static io.airlift.drift.transport.netty.client.DriftNettyMethodInvokerFactory.createStaticDriftNettyMethodInvokerFactory;

/**
 * Thrift连接工厂类
 * factory to create thrift connect
 *
 * @author naah
 */
public class DriftClientFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriftClientFactory.class);

    private static final int CONNECT_TIMEOUT = 2000;
    private static final int REQUEST_TIMEOUT = 2000;
    private static final ThriftCodecManager THRIFT_CODEC_MANAGER = new ThriftCodecManager();
    private static DriftNettyMethodInvokerFactory<?> INVOKER_FACTORY = null;

    /**
     * 选择传输协议和序列化方式
     * choose transport and protocol
     *
     * @param protocolModel
     * @param transportModel
     * @param serverNode
     * @param properties
     * @return
     */
    public static io.airlift.drift.client.DriftClientFactory determineTTranportAndProtocol(String protocolModel, String transportModel, DriftServerNode serverNode, DriftClientPoolProperties properties) {
        DriftNettyClientConfig driftNettyClientConfig = new DriftNettyClientConfig();
        Double connectTimeout = null;
        Double requestTimeout = null;

        if (properties != null) {
            connectTimeout = Double.valueOf(properties.getConnectTimeout());
            requestTimeout = Double.valueOf(properties.getRequestTimeout());
        } else {
            connectTimeout = Double.valueOf(CONNECT_TIMEOUT);
            requestTimeout = Double.valueOf(REQUEST_TIMEOUT);

        }
        driftNettyClientConfig.setConnectTimeout(new Duration(connectTimeout, TimeUnit.MILLISECONDS));
        driftNettyClientConfig.setRequestTimeout(new Duration(requestTimeout, TimeUnit.MILLISECONDS));

        switch (protocolModel) {
            case DriftProtocolModel.PROTOCOL_BINARY:
                driftNettyClientConfig.setProtocol(Protocol.BINARY);
                break;

            case DriftProtocolModel.PROTOCOL_COMPACT:
                driftNettyClientConfig.setProtocol(Protocol.COMPACT);
                break;

            case DriftProtocolModel.PROTOCOL_FB_COMPACT:
                driftNettyClientConfig.setProtocol(Protocol.FB_COMPACT);
                break;

            default:
                throw new DriftClientConfigException("Service model is configured in wrong way");
        }

        switch (transportModel) {
            case DriftTransportModel.TRANSPORT_UNFRAMED:
                driftNettyClientConfig.setTransport(Transport.UNFRAMED);
                break;

            case DriftTransportModel.TRANSPORT_FRAMED:
                driftNettyClientConfig.setTransport(Transport.FRAMED);
                break;

            case DriftTransportModel.TRANSPORT_HEADER:
                driftNettyClientConfig.setTransport(Transport.HEADER);
                break;
            default:
                throw new DriftClientConfigException("Service model is configured in wrong way");
        }

        if (INVOKER_FACTORY == null) {

            INVOKER_FACTORY = createStaticDriftNettyMethodInvokerFactory(driftNettyClientConfig);
        }

        io.airlift.drift.client.DriftClientFactory clientFactory = new io.airlift.drift.client.DriftClientFactory(
                THRIFT_CODEC_MANAGER,
                INVOKER_FACTORY,
                new SimpleAddressSelector(ImmutableList.of(HostAndPort.fromParts(serverNode.getHost(), serverNode.getPort()))),
                ExceptionClassifier.NORMAL_RESULT);

        return clientFactory;
    }

    public static io.airlift.drift.client.DriftClientFactory determineTTranportAndProtocol(String protocolModel, String transportModel, DriftServerNode serverNode) {
        /**
         * default try doing 10 times
         */
        return determineTTranportAndProtocol(protocolModel, transportModel, serverNode, null);
    }


}
