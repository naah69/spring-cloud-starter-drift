package com.example.demo;

import com.naah69.rpc.drift.client.annotation.EnableDriftClient;
import com.naah69.rpc.drift.client.annotation.ThriftRefer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author naah
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableDriftClient
public class DemoApplication {

    @ThriftRefer(version = "1.2.1")
    public static NLU nlu;

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(DemoApplication.class, args);
        getStatus();
    }

    public static String getStatus() {
        return nlu.status();
    }
}
