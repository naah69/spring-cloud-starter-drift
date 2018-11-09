package com.naah69.rpc.drift.client.scanner;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.*;

/**
 * bean 扫描配置器
 * 动态代理类的工厂类
 *
 * @author naah
 */
public class DriftClientBeanScannerConfigurer implements ApplicationContextAware, BeanFactoryPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriftClientBeanScannerConfigurer.class);

    private static final String SPRING_THRIFT_CLIENT_PACKAGE_TO_SCAN = "spring.drift.client.package-to-scan";

    private static final String DEFAULT_SCAN_PACKAGE = "";

    private ApplicationContext applicationContext;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 获取bean的定义信息，并修改bean的定义信息
     * 千万不要实例化
     *
     * @param beanFactory
     * @throws BeansException
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        BeanDefinitionRegistry definitionRegistry = (BeanDefinitionRegistry) beanFactory;
        /**
         *初始化Thrift的bean扫描类
         */
        DriftClientBeanScanner beanScanner = new DriftClientBeanScanner(definitionRegistry);
        beanScanner.setResourceLoader(applicationContext);
        beanScanner.setBeanNameGenerator(new AnnotationBeanNameGenerator());
        beanScanner.setScopedProxyMode(ScopedProxyMode.INTERFACES);

        setScannedPackages(beanScanner, applicationContext.getEnvironment().getProperty(SPRING_THRIFT_CLIENT_PACKAGE_TO_SCAN));
    }

    /**
     * 扫描包
     *
     * @param beanScanner
     * @param basePackages
     */
    private void setScannedPackages(DriftClientBeanScanner beanScanner, String basePackages) {
        /**
         * 判断基础包
         */
        if (StringUtils.isBlank(basePackages)) {
            beanScanner.scan(DEFAULT_SCAN_PACKAGE);
            return;
        }

        /**
         * 寻找第一个逗号
         */
        int delimiterIndex = StringUtils.indexOf(basePackages, ",");

        /**
         * 包含多个逗号
         */
        if (delimiterIndex > -1) {

            /**
             * 逗号分隔
             */
            StringTokenizer tokenizer = new StringTokenizer(basePackages, ",");

            Set<String> packageToScanSet = new HashSet<>();

            while (tokenizer.hasMoreTokens()) {
                String subPackage = tokenizer.nextToken();
                packageToScanSet.add(subPackage);
                LOGGER.info("Subpackage {} is to be scanned by {}", subPackage, beanScanner);
            }

            List<String> packageToScanList = new ArrayList<>(packageToScanSet);
            String[] packagesToScan = packageToScanList.toArray(new String[packageToScanList.size()]);

            /**
             * 扫描包
             */
            beanScanner.scan(packagesToScan);
        } else {
            /**
             * 如果只有一个，只扫描这一个
             */
            LOGGER.info("Base package {} is to be scanned by {}", basePackages, beanScanner);
            beanScanner.scan(basePackages);
        }
    }

}
