package com.naah69.rpc.drift.client;

import com.naah69.rpc.drift.client.annotation.ThriftRefer;
import com.naah69.rpc.drift.client.common.DriftClientContext;
import com.naah69.rpc.drift.client.exception.DriftClientInstantiateException;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Bean初始化的aop
 * 用来给{@link ThriftRefer} 注入bean 的
 *
 * @author naah
 */
public class DriftClientBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriftClientBeanPostProcessor.class);

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    /**
     * bean初始化之前
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Object target = bean;
        /**
         * 判断是否为JDK 动态代理
         */
        target = getTargetByJdkDynamicProxy(target);

        /**
         * 判断是否为Cglib代理类
         */
        target = getTargetByCglibProxy(target);

        Class<?> targetClass = target.getClass();
        final Object targetBean = target;


        /**
         * 设置对象的所有字段的值
         */
        ReflectionUtils.doWithFields(targetClass, field -> {

            /**
             *获取 @ThriftRefer
             */
            ThriftRefer thriftRefer = AnnotationUtils.findAnnotation(field, ThriftRefer.class);

            /**
             * 获取value值，value为空获取name
             */
            String referName = StringUtils.isNotBlank(thriftRefer.value()) ? thriftRefer.value() : thriftRefer.name();

            Class<?> fieldType = field.getType();
            Object injectedBean;

            /**
             *referName 不为空
             */
            if (StringUtils.isNotBlank(referName)) {
                /**
                 * 获取bean
                 */
                injectedBean = applicationContext.getBean(fieldType, referName);

                /**
                 * 创建对象，存在就创建，不存在就抛异常
                 */
                injectedBean = Optional.ofNullable(injectedBean)
                        .orElseThrow(() -> new DriftClientInstantiateException("Detected non-qualified bean with name {}" + referName));

                /**
                 * 设置该字段为可访问
                 */
                ReflectionUtils.makeAccessible(field);

                /**
                 * targetBean的field字段的值设置为targetBean
                 */
                ReflectionUtils.setField(field, targetBean, injectedBean);
            } else {
                /**
                 * 初始化context
                 */
                try {
                    DriftClientContext context = DriftClientContext.context();
                    if (context.getClient() == null) {
                        DriftClientContext.context(applicationContext);
                    }
                } catch (Exception e) {
                    LOGGER.error("", e);
                }

                Map<String, ?> injectedBeanMap = applicationContext.getBeansOfType(field.getType());
                if (MapUtils.isEmpty(injectedBeanMap)) {
                    throw new DriftClientInstantiateException("Detected non-qualified bean of {}" + fieldType.getSimpleName());
                }

                if (injectedBeanMap.size() > 1) {
                    throw new DriftClientInstantiateException("Detected ambiguous beans of {}" + fieldType.getSimpleName());
                }

                injectedBean = injectedBeanMap.entrySet().stream()
                        .findFirst()
                        .map(Map.Entry::getValue)
                        .orElseThrow(() -> new DriftClientInstantiateException(
                                "Detected non-qualified bean of {}" + fieldType.getSimpleName()));

                ReflectionUtils.makeAccessible(field);


                /**
                 * 将jdk代理对象 通过反射设置到 字段上
                 * {@link DriftClientFactoryBean 生成的}
                 */
                ReflectionUtils.setField(field, targetBean, injectedBean);
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Bean {} is injected into target bean {}, field {}", injectedBean.getClass(), targetBean.getClass(), field.getName());
            }
            /**
             * 该字段值不为空的
             */
        }, field -> (AnnotationUtils.getAnnotation(field, ThriftRefer.class) != null));

        /**
         *定义 方法过滤器，参数>0 并且返回值为空
         */
        ReflectionUtils.MethodFilter methodFilter = method -> {
            boolean basicCondition = AnnotationUtils.getAnnotation(method, ThriftRefer.class) != null
                    && method.getParameterCount() > 0
                    && method.getReturnType() == Void.TYPE;

            if (!basicCondition) {
                return false;
            }

            return false;
        };

        /**
         * 回调
         */
        ReflectionUtils.doWithMethods(targetClass, method -> {
            Parameter[] parameters = method.getParameters();

            /**
             * 遍历参数列表
             */
            Object objectArray = Arrays.stream(parameters).map(parameter -> {
                /**
                 * 参数类型
                 */
                Class<?> parameterType = parameter.getType();
                /**
                 * 获取Bean
                 */
                Map<String, ?> injectedBeanMap = applicationContext.getBeansOfType(parameterType);


                if (MapUtils.isEmpty(injectedBeanMap)) {
                    throw new DriftClientInstantiateException("Detected non-qualified bean of {}" + parameterType.getSimpleName());
                }

                if (injectedBeanMap.size() > 1) {
                    throw new DriftClientInstantiateException("Detected ambiguous beans of {}" + parameterType.getSimpleName());
                }

                /**
                 * 返回该值
                 */
                return injectedBeanMap.entrySet().stream()
                        .findFirst()
                        .map(Map.Entry::getValue)
                        .orElseThrow(() -> new DriftClientInstantiateException(
                                "Detected non-qualified bean of {}" + parameterType.getSimpleName()));
            }).collect(Collectors.toList()).toArray();

            /**
             * 设置为开放
             */
            ReflectionUtils.makeAccessible(method);
            /**
             *调用targetBean的method方法  参数为objectArray
             */
            ReflectionUtils.invokeMethod(method, targetBean, objectArray);
        }, methodFilter);

        return bean;
    }

    private Object getTargetByCglibProxy(Object target) {
        if (AopUtils.isCglibProxy(target)) {
            /**
             * 获取目标源对象
             */
            TargetSource targetSource = ((Advised) target).getTargetSource();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Target object {} uses cglib proxy");
            }

            try {
                /**
                 * 获取真正的对象
                 */
                target = targetSource.getTarget();
            } catch (Exception e) {
                throw new DriftClientInstantiateException("Failed to get target bean from " + target, e);
            }
        }
        return target;
    }

    private Object getTargetByJdkDynamicProxy(Object target) {
        /**
         * 判断是否为JAVA动态代理对象
         */
        if (AopUtils.isJdkDynamicProxy(target)) {

            /**
             * 获取目标源对象
             */
            TargetSource targetSource = ((Advised) target).getTargetSource();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Target object {} uses jdk dynamic proxy");
            }

            try {
                /**
                 * 获取真正的对象
                 */
                target = targetSource.getTarget();
            } catch (Exception e) {
                throw new DriftClientInstantiateException("Failed to get target bean from " + target, e);
            }
        }
        return target;
    }

    /**
     * bean初始化之后
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

}
