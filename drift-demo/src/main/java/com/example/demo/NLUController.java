package com.example.demo;

import com.naah69.rpc.drift.client.annotation.ThriftRefer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/nlu")
public class NLUController {

    @Autowired
    DiscoveryClient client;

    @ThriftRefer(version = "1.2.0")
    NLU nlu;

    @GetMapping("/status")
    public String status() {
        return nlu.status();
    }

    @GetMapping("/parse")
    public String parse() {
        return nlu.parse("1001_2002", "1.0.0", "哈哈哈");
    }

}
