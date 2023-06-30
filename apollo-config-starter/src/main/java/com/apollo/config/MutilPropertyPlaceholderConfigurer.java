package com.apollo.config;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.apollo.beanfactory.BeanOriginalConfig;
import com.apollo.init.TargetObjectBeanManager;
import com.ctrip.framework.foundation.internals.ServiceBootstrap;
import com.ctrip.framework.foundation.spi.ProviderManager;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

/**
 * 自定义配置属性加载
 *
 * @author weiteng
 * @date 2019-08-30
 */

public class MutilPropertyPlaceholderConfigurer extends PropertySourcesPlaceholderConfigurer {

    private static final String FILE_PATH = "file.path";
    private static Properties properties = new Properties();
    /**
     * 删除key管理
     */
    private static Set<String> deletedKey = new HashSet<>();

    /**
     * 初始化远程配置到本地属性中，配合spring进行注解使用
     *
     * @param p
     */
    public static void initRemoteConfig(Properties p) {
        String namespaceStr = p.getProperty(BeanOriginalConfig.APOLLO_BOOTSTRAP_NAMESPACENES);
        if (StringUtils.isEmpty(namespaceStr)) {
            //没有配置走默认namespace
            namespaceStr = BeanOriginalConfig.DEFAULT_NAMESPACE;
        }
        String[] namespaces = namespaceStr.split(",");
        for (String namespace : namespaces) {
            ApolloConfigUtil apolloConfigUtil = new ApolloConfigUtil(namespace);
            Map<String, String> map = apolloConfigUtil.getConfigs();
            if (map == null || map.size() <= 0) {
                continue;
            }
            map.entrySet().stream().forEach(e -> {
                p.put(e.getKey(), e.getValue());
            });
        }
        MutilPropertyPlaceholderConfigurer.properties.putAll(p);
    }

    public static String getPropert(String key) {
        return properties.getProperty(key);
    }

    public static void setPropert(String key, String value) {
        if (!properties.containsKey(key) && !deletedKey.contains(key)) {
            return;
        }
        if (value == null || "null".equals(value)) {
            deletedKey.add(key);
            properties.remove(key);
            return;
        }
        properties.put(key, value);
    }

    @Override
    protected Properties mergeProperties() throws IOException {
        Properties mergeProperties = super.mergeProperties();
        properties.putAll(mergeProperties);

        String filePath = mergeProperties.getProperty(FILE_PATH);
        if (StringUtils.isEmpty(filePath)) {
            filePath = "META-INF/app.properties";
        }
        String[] filePaths = filePath.split(",");
        for (String p : filePaths) {
            setLocation(new ClassPathResource(p));
        }
        properties.putAll(super.mergeProperties());

        this.setBeanOriginalConfig(properties);
        initRemoteConfig(properties);
        this.setProperties(properties);
        return properties;
    }

    /**
     * 设置基础配置
     *
     * @param properties
     * @throws Exception
     */
    private void setBeanOriginalConfig(Properties properties) throws RuntimeException {
        String env = System.getProperty("spring.profiles.active");
        String applicationName = properties.getProperty("spring.application.name", "t-applicationName");
        ProviderManager manager = (ProviderManager) ServiceBootstrap.loadFirst(ProviderManager.class);
        String url = manager.getProperty(env + ".meta", (String) null);
        BeanOriginalConfig.getBeanOriginalConfig().setAppName(applicationName);
        BeanOriginalConfig.getBeanOriginalConfig().setMetaUrl(url);
        BeanOriginalConfig.getBeanOriginalConfig().setClusterName(properties.getProperty(BeanOriginalConfig.APP_CLUSTER_NAME));
        BeanOriginalConfig.getBeanOriginalConfig().setEnv(env);
        //开始初始化具体对象
        TargetObjectBeanManager.setBeanOriginalConfig(BeanOriginalConfig.getBeanOriginalConfig());
    }

}
