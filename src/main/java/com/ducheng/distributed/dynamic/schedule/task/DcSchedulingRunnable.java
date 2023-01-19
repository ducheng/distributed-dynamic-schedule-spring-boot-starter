package com.ducheng.distributed.dynamic.schedule.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * 线程池真正的执行类
 */
public class DcSchedulingRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(DcSchedulingRunnable.class);

    private Object bean;         //类对象
    private String beanName;     //类名称
    private String methodName;   //方法名称
    private String taskId;       //taskId
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public DcSchedulingRunnable(Object bean, String beanName, String methodName) {
        this.bean = bean;
        this.beanName = beanName;
        this.methodName = methodName;
    }

    @Override
    public void run() {
        try {
            Method method = bean.getClass().getDeclaredMethod(methodName);
            ReflectionUtils.makeAccessible(method);
            logger.info("定时任务开始执行 类名称:{}, 方法:{}, taskId:{}",beanName, method.getName(),getTaskId());
            method.invoke(bean);
        } catch (Exception e) {
            logger.error("定时任务访问报错: {} ", e.getMessage());
        }
    }
    public String getTaskId() {
        return  beanName + "_" + methodName;
    }
}
