package com.naah69.rpc.drift.client.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 标注为接口
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ThriftClient {

    @AliasFor("name")
    String value() default "";

    @AliasFor("value")
    String name() default "";

    /**
     * 注册的服务名
     */
    String serviceId();

}
