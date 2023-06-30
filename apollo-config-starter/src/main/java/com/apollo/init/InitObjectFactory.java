package com.apollo.init;

import java.util.Map;

/**
 * 创建对象必须继承实现该类，负责创建具体目标对象
 *
 * @author weiteng
 * @date 2019-08-28
 */
public interface InitObjectFactory {

    /**
     * 初始化对象
     *
     * @param nameSpace
     * @param isGray    是否灰度创建
     * @param configs
     * @return
     */
    Object init(String nameSpace, Boolean isGray, Object targetObject, Map<String, String> configs);

}
