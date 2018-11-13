package com.example.demo;

import com.naah69.rpc.drift.client.annotation.ThriftClient;
import io.airlift.drift.annotations.ThriftMethod;
import io.airlift.drift.annotations.ThriftService;


@ThriftClient(serviceId = "nlu_service")
//@ThriftService
public interface NLU {

    @ThriftMethod("parse")
    String parse(String scene, String version, String text);

    @ThriftMethod("status")
    String status();
}
