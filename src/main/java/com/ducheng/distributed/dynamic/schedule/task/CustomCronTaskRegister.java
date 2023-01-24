package com.ducheng.distributed.dynamic.schedule.task;

import com.ducheng.distributed.dynamic.schedule.common.ConstantsPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.config.CronTask;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 *  定时任务注册器
 */
public class CustomCronTaskRegister implements DisposableBean {

    private static Logger logger = LoggerFactory.getLogger(CustomCronTaskRegister.class);


    @Resource(name = "dynamic-schedule-taskScheduler")
    private TaskScheduler taskScheduler;


    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    public void addCronTask(String taskId, String cronExpression) {
        if (null != ConstantsPool.TASK_CONCURRENT_HASH_MAP.get(taskId)) {
            removeCronTask(taskId);
        }
        DcSchedulingRunnable dcSchedulingRunnable = ConstantsPool.RUNNABLE_MAP.get(taskId);
        CronTask cronTask = new CronTask(dcSchedulingRunnable, cronExpression);
        ConstantsPool.TASK_CONCURRENT_HASH_MAP.put(taskId, scheduleCronTask(cronTask));
        logger.info("添加定时任务成功，定时任务的cron表达式:{}, taskId:{}",cronExpression,taskId);
    }



    public void removeCronTask(String taskId) {
        ScheduledTask scheduledTask = ConstantsPool.TASK_CONCURRENT_HASH_MAP.remove(taskId);
        if (scheduledTask == null) return;
        scheduledTask.cancel();
        logger.info("取消定时任务成功, taskId :{}",taskId);
    }

    private ScheduledTask scheduleCronTask(CronTask cronTask) {
        ScheduledTask scheduledTask = new ScheduledTask();
        scheduledTask.future = this.taskScheduler.schedule(cronTask.getRunnable(), cronTask.getTrigger());
        return scheduledTask;
    }

    @Override
    public void destroy() {
        ConstantsPool.TASK_CONCURRENT_HASH_MAP.clear();
        ConstantsPool.RUNNABLE_MAP.clear();
        Collection<List<String>> values = ConstantsPool.PROPERTIES_TASK_IDS.values();
        values.stream().forEach(x->{
            x.stream().forEach(y-> {
                stringRedisTemplate.delete(y);
            });
        });
        ConstantsPool.PROPERTIES_TASK_IDS.clear();
    }
}
