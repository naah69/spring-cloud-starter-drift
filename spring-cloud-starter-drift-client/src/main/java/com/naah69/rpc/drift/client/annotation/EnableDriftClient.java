package com.naah69.rpc.drift.client.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启DriftClient注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(DriftClientConfigurationSelector.class)
public @interface EnableDriftClient {
}
