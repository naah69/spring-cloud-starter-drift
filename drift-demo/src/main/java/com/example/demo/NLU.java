package com.example.demo;

import com.naah69.rpc.drift.client.annotation.ThriftClient;
import io.airlift.drift.annotations.ThriftMethod;
import io.airlift.drift.annotations.ThriftService;

@ThriftService
@ThriftClient(serviceId = "nlu_service")
public interface NLU {

    @ThriftMethod("parse")
    String parse(String scene, String version, String text);

    @ThriftMethod("status")
    String status();
}
