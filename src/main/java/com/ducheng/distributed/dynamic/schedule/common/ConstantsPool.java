package com.ducheng.distributed.dynamic.schedule.common;


import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.ducheng.distributed.dynamic.schedule.task.DcSchedulingRunnable;
import com.ducheng.distributed.dynamic.schedule.task.ScheduledTask;


/**
 *  常量池工具类
 */
public class ConstantsPool {

    /**
     *  key taskId , value Task
     */
    public static final Map<String, ScheduledTask> TASK_CONCURRENT_HASH_MAP = new ConcurrentHashMap<>(16);

    /**
     * key cronExpression value List<TaskIds>
     */
    public static final Map<String, List<String>> PROPERTIES_TASK_IDS = new ConcurrentHashMap<>(16);


    /**
     * key taskId value DcSchedulingRunnable
     */
    public static final Map<String, DcSchedulingRunnable> RUNNABLE_MAP = new ConcurrentHashMap<>(16);
}
