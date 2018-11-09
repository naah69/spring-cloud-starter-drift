package com.naah69.rpc.drift.client.scanner;

import com.naah69.rpc.drift.client.annotation.ThriftClient;
import com.naah69.rpc.drift.client.common.DriftClientDefinitionProperty;
import com.naah69.rpc.drift.client.common.DriftServiceSignature;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Set;

/**
 * bean扫描器
 *
 * @author naah
 */
public final class DriftClientBeanScanner extends ClassPathBeanDefinitionScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriftClientBeanScanner.class);

    public DriftClientBeanScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    /**
     * 过滤Bean
     */
    @Override
    protected void registerDefaultFilters() {
        this.addIncludeFilter(new AnnotationTypeFilter(ThriftClient.class));
    }

    /**
     * 扫描
     *
     * @param basePackages 路径
     * @return
     */
    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        /**
         *获取对应bean的BeanDefinitionHolder
         */
        Set<BeanDefinitionHolder> definitionHolders = super.doScan(basePackages);
        LOGGER.info("Packages scanned by thriftClientBeanDefinitionScanner is [{}]",
                StringUtils.join(basePackages, ", "));

        for (BeanDefinitionHolder definitionHolder : definitionHolders) {
            GenericBeanDefinition definition = (GenericBeanDefinition) definitionHolder.getBeanDefinition();

            LOGGER.info("Scanned and found drift client, bean {} assigned from {}",
                    definitionHolder.getBeanName(),
                    definition.getBeanClassName());

            Class<?> beanClass;
            try {
                /**
                 *获取class
                 */
                beanClass = Class.forName(definition.getBeanClassName());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                continue;
            }

            /**
             *获取class的注解
             */
            ThriftClient thriftClient = AnnotationUtils.findAnnotation(beanClass, ThriftClient.class);
            if (thriftClient == null) {
                LOGGER.warn("Thrift client is not found");
                continue;
            }

            /**
             *通过注解的值获取beanname
             */
            String beanName = StringUtils.isNotBlank(thriftClient.value())
                    ? thriftClient.value()
                    : (StringUtils.isNotBlank(thriftClient.name()) ? thriftClient.name() : StringUtils.uncapitalize(beanClass.getSimpleName()));

            /**
             *设置definition的值(向下面的ThriftClientFactoryBean注入属性)
             */
            definition.getPropertyValues().addPropertyValue(DriftClientDefinitionProperty.BEAN_NAME, beanName);
            definition.getPropertyValues().addPropertyValue(DriftClientDefinitionProperty.BEAN_CLASS, beanClass);
            definition.getPropertyValues().addPropertyValue(DriftClientDefinitionProperty.BEAN_CLASS_NAME, beanClass.getName());
            /**
             *创建服务端 签名
             */
            DriftServiceSignature serviceSignature = new DriftServiceSignature(
                    thriftClient.serviceId(), beanClass);
            /**
             *设置bean相关信息
             */
            definition.getPropertyValues().addPropertyValue(DriftClientDefinitionProperty.SERVICE_SIGNATURE, serviceSignature);
            /**
             *设置生成代理类的工厂类
             */
            definition.setBeanClass(DriftClientFactoryBean.class);
        }

        return definitionHolders;
    }


    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        AnnotationMetadata metadata = beanDefinition.getMetadata();
        return metadata.hasAnnotation(ThriftClient.class.getName())
                && metadata.isInterface();
    }


}
