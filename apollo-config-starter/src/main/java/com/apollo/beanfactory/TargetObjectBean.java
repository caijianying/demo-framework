package com.apollo.beanfactory;

import com.apollo.config.InitConfig;
import com.apollo.init.TargetObjectBeanManager;
import org.springframework.beans.factory.FactoryBean;

/**
 * 目标对象的bean工厂，负责创建具体目标对象
 *
 * @author weiteng
 * @date 2019-08-28
 */
public class TargetObjectBean implements FactoryBean {


    /**
     * 具体配置key
     */
    private String namspace;

    /**
     * 目标对象全名称
     */
    private String targetObjectClassName;

    /**
     * 目标对象类型
     */
    private Class<?> targetObjectClassType;

    public TargetObjectBean(String namspace, String targetObjectClassName) {
        this.namspace = namspace;
        this.targetObjectClassName = targetObjectClassName;
        InitConfig.addNamespace(this.namspace);
    }

    @Override
    public Object getObject() throws Exception {
        return TargetObjectBeanManager.getTargetObject(this.namspace);
    }

    @Override
    public Class<?> getObjectType() {
        try {
            if (this.targetObjectClassType != null) {
                return this.targetObjectClassType;
            }
            this.targetObjectClassType = Class.forName(this.targetObjectClassName);
            return this.targetObjectClassType;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
