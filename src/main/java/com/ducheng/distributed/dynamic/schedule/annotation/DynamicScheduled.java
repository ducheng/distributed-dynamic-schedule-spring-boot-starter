package com.ducheng.distributed.dynamic.schedule.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  自定义的动态定时任务注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DynamicScheduled {

    //描述
    String desc() ;

    // cron 表达式
    String cron();
}
