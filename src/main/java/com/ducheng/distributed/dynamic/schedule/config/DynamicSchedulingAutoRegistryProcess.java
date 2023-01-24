package com.ducheng.distributed.dynamic.schedule.config;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.ducheng.distributed.dynamic.schedule.utils.SpringUtils;
import com.ducheng.distributed.dynamic.schedule.utils.StrUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.ducheng.distributed.dynamic.schedule.common.ConstantsPool;
import com.ducheng.distributed.dynamic.schedule.annotation.DynamicScheduled;
import com.ducheng.distributed.dynamic.schedule.task.CustomCronTaskRegister;
import com.ducheng.distributed.dynamic.schedule.task.DcSchedulingRunnable;

public class DynamicSchedulingAutoRegistryProcess implements BeanPostProcessor, CommandLineRunner {

    @Autowired
    private NacosConfigProperties nacosConfigProperties;

    @Value("${spring.cloud.nacos.config.distributed.dynamic.schedule-id}")
    private String dataId;

    @Autowired
    private RedissonClient redissonClient;


    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(bean.getClass());
        if (methods == null) return bean;
        for (Method method : methods) {
            DynamicScheduled dcsScheduled = AnnotationUtils.findAnnotation(method, DynamicScheduled.class);
            //初始化加载定时任务
            if (!ObjectUtils.isEmpty(dcsScheduled)) {
                String resolve = StrUtil.resolveKey(dcsScheduled.cron());
                DcSchedulingRunnable schedulingRunnable = new DcSchedulingRunnable(bean,beanName,method.getName());
                //把他放到缓存里面
                if (!ConstantsPool.PROPERTIES_TASK_IDS.containsKey(resolve)) {
                    List<String> list = new ArrayList<>();
                    list.add(schedulingRunnable.getTaskId());
                    ConstantsPool.PROPERTIES_TASK_IDS.put(resolve,list);
                } else {
                    List<String> list = ConstantsPool.PROPERTIES_TASK_IDS.get(resolve);
                    list.add(schedulingRunnable.getTaskId());
                }
                ConstantsPool.RUNNABLE_MAP.put(schedulingRunnable.getTaskId(),schedulingRunnable);
                //customCronTaskRegister.addCronTask(schedulingRunnable.getTaskId(),resolve);
            }
        }
        return bean;
    }

    @Bean("dynamic-schedule-taskScheduler")
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        //线程池使用的就是当前线程的大小
        taskScheduler.setPoolSize(Runtime.getRuntime().availableProcessors());
        taskScheduler.setRemoveOnCancelPolicy(true);
        taskScheduler.setThreadNamePrefix("DynamicScheduleThreadPool-");
        return taskScheduler;
    }

    @Bean
    public CustomCronTaskRegister getCustomCronTaskRegister() {
        return new CustomCronTaskRegister();
    }

    @Bean
    @ConditionalOnBean(NacosConfigProperties.class)
    public DynamicSchedulingConfiguration getDynamicSchedulingConfiguration(@Autowired CustomCronTaskRegister customCronTaskRegister) {
        return new DynamicSchedulingConfiguration(customCronTaskRegister);
    }

    @Override
    public void run(String... args) throws Exception {
        ConfigService configService = NacosFactory.createConfigService(nacosConfigProperties.assembleConfigServiceProperties());
        // 程序首次启动, 并加载初始动态定时任务的配置
        String initConfigInfo = configService.getConfig(dataId, nacosConfigProperties.getGroup(), 5000);
        // 把配置文件解析成key value 的模式
        //换行符\n
        String lines[] = initConfigInfo.split("\\r?\\n");
        String toJSONString = JSONObject.toJSONString(lines);
        RLock lock = redissonClient.getLock(toJSONString);
        lock.lock();
        try {
            //在转成map
            for (String str: lines) {
                String[] split = str.split(": ");
                if (ConstantsPool.PROPERTIES_TASK_IDS.containsKey(split[0])) {
                    List<String> taskIds  = ConstantsPool.PROPERTIES_TASK_IDS.get(split[0]);
                    String cronExpression = split[1];
                    addTask(taskIds,cronExpression);
                }
            }
        }finally {
            lock.unlock();
        }
    }

    /**
     * 添加到定时任务
     * @param taskIds
     * @param cronExpression
     */
    public void addTask(List<String> taskIds,String cronExpression) {
        taskIds.stream().forEach(x-> {
            Boolean aBoolean = stringRedisTemplate.hasKey(x);
            if (!aBoolean) {
                SpringUtils.getBean(CustomCronTaskRegister.class).addCronTask(x,cronExpression);
                stringRedisTemplate.opsForValue().set(x,x);
            }
        });
    }
}
