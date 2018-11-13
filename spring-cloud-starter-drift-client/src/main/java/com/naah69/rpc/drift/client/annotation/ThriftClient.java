package com.naah69.rpc.drift.client.annotation;

import io.airlift.drift.annotations.ThriftService;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 标注接口为Thrift服务
 * mark a interface as Thrift Service
 *
 * @author naah
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ThriftService
public @interface ThriftClient {

    @AliasFor("name")
    String value() default "";

    @AliasFor("value")
    String name() default "";

    /**
     * 注册的服务名
     * the service name that had registered in register center
     */
    String serviceId();

}
