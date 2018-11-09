package com.naah69.rpc.drift.client.properties;

/**
 * @author naah
 */
public final class DriftTransportModel {

    /**
     * FRAMED传输协议
     * 将数据封装Frame(帧)实现的， TFastFramedTransport效率内存使用率高，使用了自动扩展长度的buffer
     */
    public static final String TRANSPORT_FRAMED = "framed";

    /**
     * UNFRAMED传输协议
     * 基于BIO客户端传输类。 TSocket持有Socket，设置输入输出流使用1K的BufferedStream
     */
    public static final String TRANSPORT_UNFRAMED = "unframed";

    /**
     * HEADER传输协议
     */
    public static final String TRANSPORT_HEADER = "header";

    /**
     * 默认的传输协议
     */
    public static final String TRANSPORT_MODEL_DEFAULT = TRANSPORT_FRAMED;

}
