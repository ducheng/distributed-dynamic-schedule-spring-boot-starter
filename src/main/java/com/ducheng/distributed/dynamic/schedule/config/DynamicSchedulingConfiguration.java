package com.ducheng.distributed.dynamic.schedule.config;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.ducheng.distributed.dynamic.schedule.utils.SpringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import com.ducheng.distributed.dynamic.schedule.common.ConstantsPool;
import com.ducheng.distributed.dynamic.schedule.task.CustomCronTaskRegister;

/**
 * 这里就是一个监听器,监听nacos 配置文件动态变化的key , 在动态的刷新定时任务
 */
public class DynamicSchedulingConfiguration implements EnvironmentAware,  ApplicationListener<EnvironmentChangeEvent> {

    private static  RedissonClient redissonClient =  SpringUtils.getBean(RedissonClient.class);

    private Environment environment;

    private CustomCronTaskRegister customCronTaskRegister;

    public DynamicSchedulingConfiguration(CustomCronTaskRegister customCronTaskRegister) {
        this.customCronTaskRegister = customCronTaskRegister;
    }

    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        List<String> list = new ArrayList<String>(event.getKeys());
        //这里要加上redislock的分布式锁， 保证只有一个实例来执行。
        String toJSONString = JSONObject.toJSONString(list);
        RLock lock = redissonClient.getLock(toJSONString);
        lock.lock();
        try {
            for (String str : list) {
                if (ConstantsPool.PROPERTIES_TASK_IDS.containsKey(str)) {
                    List<String> taskIds  = ConstantsPool.PROPERTIES_TASK_IDS.get(str);
                    String cronExpression = environment.getProperty(str);
                    addTask(taskIds,cronExpression);
                }
            }
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }


    /**
     * 添加到定时任务
     * @param taskIds
     * @param cronExpression
     */
    public void addTask(List<String> taskIds,String cronExpression) {
        taskIds.stream().forEach(x-> {
            customCronTaskRegister.addCronTask(x,cronExpression);
        });
    }
}
