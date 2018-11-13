package com.naah69.rpc.drift.client.annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.commons.util.SpringFactoryImportSelector;
import org.springframework.core.env.Environment;

/**
 * 判断配置开启
 * judge config whether be enable
 *
 * @author naah
 */
public class DriftClientConfigurationSelector extends SpringFactoryImportSelector<EnableDriftClient> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriftClientConfigurationSelector.class);

    private static final String DRIFT_ENABLE = "spring.drift.enable";

    @Override
    protected boolean isEnabled() {
        Environment environment = getEnvironment();
        Boolean enable = environment.getProperty(DRIFT_ENABLE, Boolean.class);
        if (enable) {
            LOGGER.info("Enable drift client auto configuration");
        } else {
            LOGGER.warn("Disnable drift client auto configuration");
        }
        return enable.booleanValue();
    }
}
