package com.naah69.rpc.drift.client.scanner;

import com.naah69.rpc.drift.client.common.DriftServiceSignature;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cglib.core.SpringNamingPolicy;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * 客户端工厂类Bean
 * 生成代理类
 *
 * @param <T>
 * @author naah
 */
public class DriftClientFactoryBean<T> implements FactoryBean<T>, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriftClientFactoryBean.class);

    private String beanName;

    private Class<?> beanClass;

    private String beanClassName;

    private Class<?> serviceClass;

    private DriftServiceSignature serviceSignature;


    /**
     * 创建代理类
     *
     * @return
     * @throws Exception
     */
    @Override
    @SuppressWarnings("unchecked")
    public T getObject() throws Exception {

        /**
         *如果是接口就用JAVA 的 动态代理
         */
        if (beanClass.isInterface()) {
            LOGGER.info("Prepare to generate proxy for {} with JDK", beanClass.getName());


            /**
             *加一层代理(无用，为了不报错)
             */
            T t = (T) Proxy.newProxyInstance(beanClass.getClassLoader(), new Class<?>[]{beanClass}, (proxy, method, args) -> new Object());

            ProxyFactoryBean factoryBean = new ProxyFactoryBean();
            factoryBean.setTarget(t);
            factoryBean.setBeanClassLoader(getClass().getClassLoader());

            /**
             * 添加AOP拦截
             */
            DriftClientAdvice clientAdvice = new DriftClientAdvice(serviceSignature);
            factoryBean.addAdvice(clientAdvice);
            factoryBean.setProxyTargetClass(true);
            factoryBean.setSingleton(true);
            factoryBean.setOptimize(true);
            factoryBean.setFrozen(true);

            /**
             *生成代理对象
             */
            T object = (T) factoryBean.getObject();

            return object;
        } else {
            /**
             *否则用CG lib
             */
            LOGGER.info("Prepare to generate proxy for {} with Cglib", beanClass.getName());

            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(beanClass);
            enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
            enhancer.setUseFactory(true);

            /**
             *MethodInterceptor 只有一个方法的时候  可以使用下面的方法 实现该方法
             */
            MethodInterceptor callback = (target, method, args, methodProxy) -> {
                return methodProxy.invokeSuper(target, args);
            };

            enhancer.setCallback(callback);
            return (T) enhancer.create();
        }

    }

    /**
     * 获取对象类型
     *
     * @return
     */
    @Override
    public Class<?> getObjectType() {
        if (Objects.isNull(beanClass) && StringUtils.isBlank(beanName)) {
            return null;
        }

        if (Objects.nonNull(beanClass)) {
            return beanClass;
        }

        if (StringUtils.isNotBlank(beanClassName)) {
            try {
                beanClass = Class.forName(beanClassName);
                return beanClass;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
        }

        return null;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        LOGGER.info("Succeed to instantiate an instance of DriftClientFactoryBean: {}", this);
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public Class<?> getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }

    public DriftServiceSignature getServiceSignature() {
        return serviceSignature;
    }

    public void setServiceSignature(DriftServiceSignature serviceSignature) {
        this.serviceSignature = serviceSignature;
    }


}
