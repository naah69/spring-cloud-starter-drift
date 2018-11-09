package com.naah69.rpc.drift.client;

import com.naah69.rpc.drift.client.common.DriftClientContext;
import com.naah69.rpc.drift.client.pool.DriftClientFactoryKeyedPooledObjectFactory;
import com.naah69.rpc.drift.client.pool.DriftServiceKeyedObjectPool;
import com.naah69.rpc.drift.client.properties.DriftClientPoolProperties;
import com.naah69.rpc.drift.client.properties.DriftClientProperties;
import com.naah69.rpc.drift.client.properties.DriftClientPropertiesCondition;
import com.naah69.rpc.drift.client.scanner.DriftClientBeanScannerConfigurer;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * 自动配置类
 *
 * @author naah
 */
@Configuration
@Conditional(value = {DriftClientPropertiesCondition.class})
@EnableConfigurationProperties(DriftClientProperties.class)
@AutoConfigureAfter(JacksonAutoConfiguration.class)
public class DriftClientAutoConfiguration implements ApplicationContextAware {

    private ApplicationContext applicationContext;


    /**
     * 创建扫描器配置类
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public DriftClientBeanScannerConfigurer driftClientBeanScannerConfigurer() {
        return new DriftClientBeanScannerConfigurer();
    }

    /**
     * 对象池配置
     *
     * @param properties
     * @return
     */
    @Bean
    public GenericKeyedObjectPoolConfig keyedObjectPoolConfig(DriftClientProperties properties) {
        DriftClientPoolProperties poolProperties = properties.getPool();

        /**
         * 对象池设置参数
         */
        GenericKeyedObjectPoolConfig config = new GenericKeyedObjectPoolConfig();
        config.setMinIdlePerKey(poolProperties.getPoolMinIdlePerKey());
        config.setMaxIdlePerKey(poolProperties.getPoolMaxIdlePerKey());
        config.setMaxWaitMillis(poolProperties.getPoolMaxWait());
        config.setMaxTotalPerKey(poolProperties.getPoolMaxTotalPerKey());
        config.setTestOnCreate(poolProperties.isTestOnCreate());
        config.setTestOnBorrow(poolProperties.isTestOnBorrow());
        config.setTestOnReturn(poolProperties.isTestOnReturn());
        config.setTestWhileIdle(poolProperties.isTestWhileIdle());
        config.setFairness(true);
        config.setJmxEnabled(false);
        return config;
    }

    /**
     * 传输对象池工工厂
     *
     * @param properties
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public DriftClientFactoryKeyedPooledObjectFactory transportKeyedPooledObjectFactory(
            DriftClientProperties properties) {
        return new DriftClientFactoryKeyedPooledObjectFactory(properties);
    }

    /**
     * 传输对象池
     *
     * @param config      对象池配置 {@link GenericKeyedObjectPoolConfig}
     * @param poolFactory 对象池工厂类
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public DriftServiceKeyedObjectPool transportKeyedObjectPool(
            GenericKeyedObjectPoolConfig config, DriftClientFactoryKeyedPooledObjectFactory poolFactory) {
        return new DriftServiceKeyedObjectPool(poolFactory, config);
    }

    /**
     * 后置处理器
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public DriftClientBeanPostProcessor driftClientBeanPostProcessor() {
        return new DriftClientBeanPostProcessor();
    }

    /**
     * 客户端上下文
     *
     * @param properties
     * @param objectPool
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public DriftClientContext driftClientContext(
            DriftClientProperties properties, DriftServiceKeyedObjectPool objectPool) {
        return DriftClientContext.context(properties, objectPool);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
