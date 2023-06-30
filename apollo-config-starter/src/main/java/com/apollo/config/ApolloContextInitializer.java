package com.apollo.config;

import com.apollo.beanfactory.BeanOriginalConfig;
import com.apollo.init.TargetObjectBeanManager;
import com.ctrip.framework.apollo.spring.config.PropertySourcesConstants;
import com.ctrip.framework.foundation.internals.ServiceBootstrap;
import com.ctrip.framework.foundation.spi.ProviderManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @author liguang
 * @date 2023/6/29 星期四 4:28 下午
 */
public class ApolloContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>,
    Ordered {
    @Override
    public void initialize(ConfigurableApplicationContext context) {
        ConfigurableEnvironment environment = context.getEnvironment();
        if (environment.getPropertySources().contains(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME)){
            //already initialized
            return;
        }
        String env = System.getProperty("spring.profiles.active");
        if (StringUtils.isBlank(env)) {
            throw new RuntimeException("env is empty");
        }

        ProviderManager manager = (ProviderManager) ServiceBootstrap.loadFirst(ProviderManager.class);
        String url = manager.getProperty(env + ".meta", (String) null);

        setBeanOriginalConfig(environment,url,env);
        addCompositePropertySource(environment);
    }

    @Override
    public int getOrder() {
        return -10;
    }

    /**
     * 设置基础配置
     * @author liguang
     * @date 2023/6/29 4:40 下午
     * @param environment:
     * @param url:
     * @param env:
     * @return
     **/
    private void setBeanOriginalConfig(ConfigurableEnvironment environment, String url, String env){
        System.out.println("url===========:" + url);
        System.out.println("env===========:" + env);
        String applicationName = environment.getProperty(BeanOriginalConfig.APP_ID, "t-applicationName");
        BeanOriginalConfig.getBeanOriginalConfig().setAppName(applicationName);
        BeanOriginalConfig.getBeanOriginalConfig().setEnv(env);
        BeanOriginalConfig.getBeanOriginalConfig().setMetaUrl(url);
        BeanOriginalConfig.getBeanOriginalConfig().setClusterName(environment.getProperty(BeanOriginalConfig.APP_CLUSTER_NAME));
        //开始初始化具体对象
        TargetObjectBeanManager.setBeanOriginalConfig(BeanOriginalConfig.getBeanOriginalConfig());
    }

    /**
     * 添加spring boot的扩展外载资源配置
     *
     * @param environment
     */
    private void addCompositePropertySource(ConfigurableEnvironment environment) {
        String namespace = environment.getProperty(BeanOriginalConfig.APOLLO_BOOTSTRAP_NAMESPACENES,BeanOriginalConfig.DEFAULT_NAMESPACE);
        String[] namespaces = namespace.split(",");
        CompositePropertySource composite = new CompositePropertySource(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME);
        for (String n : namespaces) {
            composite.addPropertySource(new ConfigPropertySource(n, ApolloConfigManager.getConfig(n)));
        }
        environment.getPropertySources().addFirst(composite);
    }
}
