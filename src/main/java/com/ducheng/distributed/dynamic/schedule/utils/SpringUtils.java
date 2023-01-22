package com.ducheng.distributed.dynamic.schedule.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 获取bean的类型
     * @param calss
     * @param <T>
     * @return
     */
    public static <T> T  getBean(Class<T> calss) {
     return applicationContext.getBean(calss);
    }
}
