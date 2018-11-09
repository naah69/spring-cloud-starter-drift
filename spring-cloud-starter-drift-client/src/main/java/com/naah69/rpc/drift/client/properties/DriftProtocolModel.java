package com.naah69.rpc.drift.client.properties;

/**
 * @author naah
 */
public final class DriftProtocolModel {

    /**
     * BINARY序列化方式
     * 基于二进制
     */
    public static final String PROTOCOL_BINARY = "binary";

    /**
     * COMPACT序列化方式
     */
    public static final String PROTOCOL_COMPACT = "compact";

    /**
     * FB_COMPACT序列化方式
     */
    public static final String PROTOCOL_FB_COMPACT = "fb_compact";

    /**
     * 默认的序列化方式
     */
    public static final String PROTOCOL_MODEL_DEFAULT = PROTOCOL_BINARY;

}
