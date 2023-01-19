package com.ducheng.distributed.dynamic.schedule.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;
import com.ducheng.distributed.dynamic.schedule.config.DynamicSchedulingAutoRegistryProcess;

/**
 * 是否开启使用分布式的动态定时任务的注解
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({DynamicSchedulingAutoRegistryProcess.class})
public @interface EnableDistributedDynamicScheduling {
}
