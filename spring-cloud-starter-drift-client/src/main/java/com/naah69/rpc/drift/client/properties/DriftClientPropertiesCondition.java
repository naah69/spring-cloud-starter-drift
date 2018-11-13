package com.naah69.rpc.drift.client.properties;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 配置信息判断类
 * config condition
 *
 * @author naah
 */
public class DriftClientPropertiesCondition extends SpringBootCondition {

    private static final String SPRING_THRIFT_CLIENT_PACKAGE_TO_SCAN = "spring.drift.client.package-to-scan";

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String packageToScan = context.getEnvironment().getProperty(SPRING_THRIFT_CLIENT_PACKAGE_TO_SCAN);

        return new ConditionOutcome(StringUtils.isNotBlank(packageToScan)
                , "spring_thrift_client_package_to_scan exist");
    }

}
