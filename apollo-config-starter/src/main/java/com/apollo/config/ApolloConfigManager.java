package com.apollo.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.apollo.init.OpenAndCloseGraySwitch;
import com.apollo.init.TargetObjectBeanManager;
import com.apollo.spi.SelfServiceLoader;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.google.common.collect.Maps;
import org.springframework.util.StringUtils;

/**
 * apollo配置管理
 * @author liguang
 * @date 2023/6/29 星期四 4:54 下午
 */
public class ApolloConfigManager {
    /**
     * 配置对象本地缓存管理
     */
    private static final Map<String, Config> CONFIG_MAP = Maps.newConcurrentMap();

    /**
     * 配置的本地缓存
     */
    private static final Map<String, Map<String, String>> CONFIG_CACHE = Maps.newConcurrentMap();


    static {
        System.setProperty("app.id", TargetObjectBeanManager.beanOriginalConfig.getAppName());
        System.setProperty("apollo.meta", TargetObjectBeanManager.beanOriginalConfig.getMetaUrl());
        if (!StringUtils.isEmpty(TargetObjectBeanManager.beanOriginalConfig.getClusterName())) {
            System.setProperty("apollo.cluster", TargetObjectBeanManager.beanOriginalConfig.getClusterName());
        }
        if (!StringUtils.isEmpty(TargetObjectBeanManager.beanOriginalConfig.getEnv())) {
            System.setProperty("ENV", TargetObjectBeanManager.beanOriginalConfig.getEnv());
        }
    }

    /**
     * 根据命名空间获取config对象
     *
     * @param namespace
     * @return
     */
    public static Config getConfig(String namespace) {
        if (CONFIG_MAP.containsKey(namespace)) {
            return CONFIG_MAP.get(namespace);
        }
        getConfigs(namespace);
        return CONFIG_MAP.get(namespace);
    }

    /**
     * 获取指定命名空间所有配置
     *
     * @param namespace
     * @return
     */
    public static Map<String, String> getConfigs(String namespace) {
        if (CONFIG_MAP.containsKey(namespace)) {
            return convertToMap(namespace, CONFIG_MAP.get(namespace));
        }
        Config config = ConfigService.getConfig(namespace);
        if (config == null) {
            return new HashMap<String, String>();
        }
        config.addChangeListener(new ConfigChangeListener() {
            @Override
            public void onChange(ConfigChangeEvent configChangeEvent) {
                String namespace = configChangeEvent.getNamespace();

                Map<String, String> namespaceMap = changeConfig(configChangeEvent);

                //除了回滚灰度以外所有配置变更都要重新创建对象
                if (namespaceMap != null) {
                    //获取对象缓存key
                    String objectKey = namespaceMap.containsKey(TargetObjectBeanManager.OPEN_CLOSE_PRE) && namespaceMap.get(TargetObjectBeanManager.OPEN_CLOSE_PRE).contains("true") ? TargetObjectBeanManager.getNamespace(namespace) : namespace;
                    TargetObjectBeanManager.init(namespace, objectKey, namespaceMap);
                }
            }
        });
        CONFIG_MAP.put(namespace, config);
        return convertToMap(namespace, config);
    }

    /**
     * 将apollo配置转换为map
     *
     * @param namespace
     * @param config
     * @return
     */
    private static Map<String, String> convertToMap(String namespace, Config config) {
        String n = null;
        if (TargetObjectBeanManager.judgeGraySwitchFactoryIsAdd(namespace)) {
            n = TargetObjectBeanManager.getNamespace(namespace);
            n = TargetObjectBeanManager.getNamespace(namespace, CONFIG_CACHE.get(n));
        } else {
            n = namespace;
        }
        if (CONFIG_CACHE.containsKey(n)) {
            return CONFIG_CACHE.get(n);
        }
        Map<String, String> map = new HashMap<String, String>();
        Set<String> keys = config.getPropertyNames();
        for (String key : keys) {
            map.put(key, config.getProperty(key, ""));
        }
        CONFIG_CACHE.put(n, map);
        return map;
    }

    /**
     * 调整改变配置值
     *
     * @param configChangeEvent
     */
    private static Map<String, String> changeConfig(ConfigChangeEvent configChangeEvent) {
        Set<String> changeKeys = configChangeEvent.changedKeys();
        String namespace = configChangeEvent.getNamespace();

        Map<String, String> changedMap = new HashMap<>();
        Set<String> deletedMap = new HashSet<>();

        Boolean isHavingOpenGray = false;
        String openGray = "false";
        for (String key : changeKeys) {
            String v = configChangeEvent.getChange(key).getNewValue();
            try {
                //同步更新本地属性文件缓存
                MutilPropertyPlaceholderConfigurer.setPropert(key, v);
            } catch (Exception ex) {
            }
            if (v == null || "null".equals(v)) {
                deletedMap.add(key);
            } else {
                changedMap.put(key, v);
            }
            if (key.equals(TargetObjectBeanManager.OPEN_CLOSE_PRE)) {
                isHavingOpenGray = true;
                openGray = v;
            }
        }
        Map<String, String> namespaceMap = null;
        if (isHavingOpenGray) {
            String n = TargetObjectBeanManager.getNamespace(namespace);
            if (openGray.contains("true")) {
                SelfServiceLoader.load(OpenAndCloseGraySwitch.class);
                namespaceMap = CONFIG_CACHE.containsKey(n) ? CONFIG_CACHE.get(n) : new HashMap<>(CONFIG_CACHE.get(namespace));
                namespaceMap = mergeMap(namespaceMap, changedMap, deletedMap);
                CONFIG_CACHE.put(n, namespaceMap);
            } else {
                TargetObjectBeanManager.closeGraySwitch(namespace);
                CONFIG_CACHE.remove(n);
            }
            return namespaceMap;
        }
        Map<String, String> configCacheMap = CONFIG_CACHE.get(namespace);
        namespaceMap = mergeMap(configCacheMap, changedMap, deletedMap);
        CONFIG_CACHE.put(namespace, namespaceMap);
        return namespaceMap;
    }

    /**
     * 合并map
     *
     * @param namespaceMap
     * @param changeMap
     * @param deleteds
     * @return
     */
    private static Map<String, String> mergeMap(Map<String, String> namespaceMap, Map<String, String> changeMap, Set<String> deleteds) {
        namespaceMap = namespaceMap == null || namespaceMap.size() <= 0 ? new HashMap<>() : namespaceMap;
        namespaceMap.putAll(changeMap);
        for (String s : deleteds) {
            namespaceMap.remove(s);
        }
        return namespaceMap;
    }

    /**
     * 获取具体配置key对应value
     *
     * @param namespace
     * @param key
     * @return
     */
    public static String getConfigValue(String namespace, String key) {
        Map<String, String> map = getConfigs(namespace);
        return map.get(key);
    }
}
