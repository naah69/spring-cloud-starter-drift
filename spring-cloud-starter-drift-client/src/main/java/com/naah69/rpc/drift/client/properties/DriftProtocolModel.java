package com.naah69.rpc.drift.client.properties;

/**
 * 序列化模型
 * protocol model
 *
 * @author naah
 */
public final class DriftProtocolModel {

    /**
     * BINARY(二进制)
     */
    public static final String PROTOCOL_BINARY = "binary";

    /**
     * COMPACT(压缩)
     */
    public static final String PROTOCOL_COMPACT = "compact";

    /**
     * FB_COMPACT
     */
    public static final String PROTOCOL_FB_COMPACT = "fb_compact";

    /**
     * DEFAULT
     */
    public static final String PROTOCOL_MODEL_DEFAULT = PROTOCOL_BINARY;

}
