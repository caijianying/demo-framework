package com.apollo.beanfactory;

import lombok.Data;

/**
 * @author liguang
 * @date 2023/6/29 星期四 4:45 下午
 */
@Data
public class BeanOriginalConfig {

    public static final String APOLLO_BOOTSTRAP_NAMESPACENES = "demo.app.namespaces";
    public static final String APP_ID = "demo.app.id";
    public static final String DEFAULT_NAMESPACE = "application";
    public static final String APP_CLUSTER_NAME = "demo.app.cluster";
    private static final BeanOriginalConfig beanOriginalConfig = new BeanOriginalConfig();

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 环境标识
     */
    private String env;

    /**
     * 集群名称
     */
    private String clusterName;

    /**
     * 目标配置中心地址
     */
    private String metaUrl;

    public static BeanOriginalConfig getBeanOriginalConfig() {
        return beanOriginalConfig;
    }

}
