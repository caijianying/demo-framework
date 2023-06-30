package com.apollo.init;

import java.util.Map;

/**
 * 灰度开关控制器
 *
 * @author weiteng
 * @date 2019-09-03
 */
public interface OpenAndCloseGraySwitch {

    /**
     * 判断是否开启灰度开关
     *
     * @param namespace
     * @param map
     * @return
     */
    boolean judgeIsOpenGraySwitch(String namespace, Map<String, String> map);

}
