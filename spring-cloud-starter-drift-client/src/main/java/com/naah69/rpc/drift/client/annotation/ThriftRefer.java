package com.naah69.rpc.drift.client.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 注入接口
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ThriftRefer {

    @AliasFor("name")
    String value() default "";

    @AliasFor("value")
    String name() default "";

    /**
     * 版本号
     *
     * @return
     */
    String version() default "";
}
