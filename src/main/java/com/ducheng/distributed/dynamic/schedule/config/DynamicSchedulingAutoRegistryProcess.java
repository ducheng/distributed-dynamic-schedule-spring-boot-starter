package com.ducheng.distributed.dynamic.schedule.config;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.ducheng.distributed.dynamic.schedule.common.ConstantsPool;
import com.ducheng.distributed.dynamic.schedule.annotation.DynamicScheduled;
import com.ducheng.distributed.dynamic.schedule.task.CustomCronTaskRegister;
import com.ducheng.distributed.dynamic.schedule.task.DcSchedulingRunnable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class DynamicSchedulingAutoRegistryProcess implements BeanPostProcessor,InitializingBean {

    @Autowired
    private CustomCronTaskRegister customCronTaskRegister;


    @Autowired
    private NacosConfigProperties nacosConfigProperties;

    //@Value("${spring.cloud.gateway.dynamic.data-id:gateway-route}")
    private String dataId = "application-dr-config-dev.yml";


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(bean.getClass());
        if (methods == null) return bean;
        for (Method method : methods) {
            DynamicScheduled dcsScheduled = AnnotationUtils.findAnnotation(method, DynamicScheduled.class);
            //初始化加载定时任务
            if (!ObjectUtils.isEmpty(dcsScheduled)) {
                String resolve = null;
                DcSchedulingRunnable schedulingRunnable = new DcSchedulingRunnable(bean,beanName,method.getName());
                //把他放到缓存里面
                if (!ConstantsPool.PROPERTIES_TASK_IDS.containsKey(dcsScheduled.cron())) {
                    List<String> list = new ArrayList<>();
                    list.add(schedulingRunnable.getTaskId());
                    ConstantsPool.PROPERTIES_TASK_IDS.put(dcsScheduled.cron(),list);
                } else {
                    List<String> list = ConstantsPool.PROPERTIES_TASK_IDS.get(dcsScheduled.cron());
                    list.add(schedulingRunnable.getTaskId());
                }
                ConstantsPool.RUNNABLE_MAP.put(schedulingRunnable.getTaskId(),schedulingRunnable);
                customCronTaskRegister.addCronTask(schedulingRunnable.getTaskId(),resolve);
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
    public void afterPropertiesSet() throws Exception {
        ConfigService configService = NacosFactory.createConfigService(nacosConfigProperties.assembleConfigServiceProperties());
        // 程序首次启动, 并加载初始化路由配置
        String initConfigInfo = configService.getConfig(dataId, nacosConfigProperties.getGroup(), 5000);

    }
}
