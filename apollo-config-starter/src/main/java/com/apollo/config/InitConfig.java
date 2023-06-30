package com.apollo.config;

import java.util.HashSet;
import java.util.Set;

import com.apollo.init.InitObjectFactory;
import com.apollo.init.TargetObjectBeanManager;
import com.apollo.spi.SelfServiceLoader;

/**
 * 初始化配置
 *
 * @author weiteng
 * @date 2019-08-28
 */
public class InitConfig {

    /**
     * 具体对象配置管理
     */
    private static final Set<String> NAMESPACES = new HashSet<String>();

    /**
     * 添加具体配置key
     *
     * @param namespace
     */
    public static void addNamespace(String namespace) {
        //加载目标对象管理和灰度开关工厂
        SelfServiceLoader.load(InitObjectFactory.class);
        TargetObjectBeanManager.init(namespace, namespace, ApolloConfigManager.getConfigs(namespace));
    }

}
