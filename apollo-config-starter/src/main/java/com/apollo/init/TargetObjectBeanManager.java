package com.apollo.init;

import java.util.Map;

import com.apollo.beanfactory.BeanOriginalConfig;
import com.apollo.config.ApolloConfigManager;
import com.google.common.collect.Maps;

/**
 * 目标对象管理器
 * @author liguang
 * @date 2023/6/29 星期四 4:49 下午
 */
public class TargetObjectBeanManager {

    /**
     * 控制开关前缀
     */
    public static final String OPEN_CLOSE_PRE = "open_close";

    /**
     * 灰度开关控制器具体实现
     */
    private static final Map<String, OpenAndCloseGraySwitch> OPEN_CLOSE_GRAY_SWITCH_MAP = Maps.newConcurrentMap();

    /**
     * 初始化对象工厂缓存管理
     */
    private static final Map<String, InitObjectFactory> INIT_OBJECT_MAP = Maps.newConcurrentMap();

    /**
     * 具体目标对象缓存
     */
    private static final Map<String, Object> TARGET_OBJECT_BEAN = Maps.newConcurrentMap();


    /**
     * 基础配置对象
     */
    public static BeanOriginalConfig beanOriginalConfig;


    public static void setBeanOriginalConfig(BeanOriginalConfig beanOriginalConfig) {
        TargetObjectBeanManager.beanOriginalConfig = beanOriginalConfig;
    }

    /**
     * 判断对象工厂是否添加
     *
     * @param namespace
     * @return
     */
    public static Boolean judgeGraySwitchFactoryIsAdd(String namespace) {
        return OPEN_CLOSE_GRAY_SWITCH_MAP.containsKey(namespace);
    }

    /**
     * 获取namespace
     *
     * @param namespace
     * @param map
     * @return
     */
    public static String getNamespace(String namespace, Map<String, String> map) {
        if (OPEN_CLOSE_GRAY_SWITCH_MAP.containsKey(namespace)) {
            namespace = OPEN_CLOSE_GRAY_SWITCH_MAP.get(namespace).judgeIsOpenGraySwitch(namespace, map) ? getNamespace(namespace) : namespace;
        }
        return namespace;
    }

    /**
     * 获取namespace
     *
     * @param namespace
     * @return
     */
    public static String getNamespace(String namespace) {
        return TargetObjectBeanManager.OPEN_CLOSE_PRE + namespace;
    }

    /**
     * 初始化对象
     *
     * @param namespace
     * @param map       具体配置参数
     */
    public static void init(String namespace, String objectKey, Map<String, String> map) {
        InitObjectFactory initObjectFactory = INIT_OBJECT_MAP.get(namespace);
        if (initObjectFactory == null) {
            return;
        }
        Boolean isGray = !namespace.equals(objectKey);
        Object tObject = getTargetObject(namespace);
        Object object = initObjectFactory.init(namespace, isGray, tObject, map);
        if (object == null) {
            return;
        }
        TARGET_OBJECT_BEAN.put(objectKey, object);
    }

    /**
     * 添加初始化工厂对象
     *
     * @param namespace
     * @param object
     */
    public static void addInitObjectFactory(String namespace, Object object) {
        if (object instanceof InitObjectFactory) {
            INIT_OBJECT_MAP.put(namespace, (InitObjectFactory) object);
        } else {
            OPEN_CLOSE_GRAY_SWITCH_MAP.put(namespace, (OpenAndCloseGraySwitch) object);
        }
    }


    /**
     * 关闭灰度开关
     *
     * @param namespace
     */
    public static void closeGraySwitch(String namespace) {
        OPEN_CLOSE_GRAY_SWITCH_MAP.remove(namespace);
        TARGET_OBJECT_BEAN.remove(getNamespace(namespace));
    }

    /**
     * 获取目标对象
     *
     * @param namespace
     * @return
     */
    public static Object getTargetObject(String namespace) {
        return TARGET_OBJECT_BEAN.get(getNamespace(namespace, ApolloConfigManager.getConfigs(namespace)));
    }

}
