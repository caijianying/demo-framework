package com.apollo.config;

import com.apollo.beanfactory.BeanOriginalConfig;
import com.ctrip.framework.apollo.spring.config.PropertySourcesProcessor;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 阿波罗配置动态变更
 *
 * @author 阳仔
 */
@Slf4j
public class ApolloEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String namespace = environment.getProperty(BeanOriginalConfig.APOLLO_BOOTSTRAP_NAMESPACENES);
        if (StringUtils.isNotEmpty(namespace)) {
            String[] namespaces = namespace.split(",");
            //namespace 手动加到 apollo
            PropertySourcesProcessor.addNamespaces(Lists.newArrayList(namespaces), 1);
            log.info("Load {} to NAMESPACE_NAMES", namespaces);
        }
    }
}