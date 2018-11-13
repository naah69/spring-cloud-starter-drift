package com.naah69.rpc.drift.client.properties;

/**
 * 传输方式
 * transport model
 *
 * @author naah
 */
public final class DriftTransportModel {

    /**
     * FRAMED（NIO）
     */
    public static final String TRANSPORT_FRAMED = "framed";

    /**
     * UNFRAMED(IO）
     */
    public static final String TRANSPORT_UNFRAMED = "unframed";

    /**
     * HEADER
     */
    public static final String TRANSPORT_HEADER = "header";

    /**
     * DEFAULT
     */
    public static final String TRANSPORT_MODEL_DEFAULT = TRANSPORT_FRAMED;

}
