package com.apollo.config;

import java.util.HashMap;
import java.util.Map;

/**
 * apollo配置获取工具
 *
 * @author weiteng
 * @date 2019-09-02
 */
public class ApolloConfigUtil {

    /**
     * 默认集群配置全局唯一
     */
    private static final ApolloConfigUtil apolloConfigUtil;

    static {
        apolloConfigUtil = new ApolloConfigUtil("application");
    }

    /**
     * 具体命名空间，默认是application
     */
    private String namespace;

    public ApolloConfigUtil(String namespace) {
        this.namespace = namespace;
    }

    public static ApolloConfigUtil getApolloConfigUtil() {
        return ApolloConfigUtil.apolloConfigUtil;
    }

    /**
     * 获取指定配置
     *
     * @param key
     * @return
     */
    public String getConfig(String key) {
        return ApolloConfigManager.getConfigValue(this.namespace, key);
    }

    /**
     * 根据key批量获取值，如果不知道key默认获取namespace下面所有配置，谨慎使用
     *
     * @param keys
     * @return
     */
    public Map<String, String> getConfigs(String... keys) {
        Map<String, String> map = new HashMap<>();
        if (keys == null || keys.length <= 0) {
            return ApolloConfigManager.getConfigs(this.namespace);
        }
        for (String k : keys) {
            map.put(k, getConfig(k));
        }
        return map;
    }

}
