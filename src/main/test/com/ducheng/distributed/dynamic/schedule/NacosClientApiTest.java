package com.ducheng.distributed.dynamic.schedule;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 这里不需要web 环境，便于启动单元测试加速
 */
@SpringBootTest(classes = SchedulingApplication.class,webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class NacosClientApiTest {


    @Value("${spring.cloud.nacos.config.distributed.dynamic.schedule-id}")
    private String dataId;

    @Autowired
    private NacosConfigProperties nacosConfigProperties;


    @Test
    public void clientApiTest() throws NacosException {
        ConfigService configService = NacosFactory.createConfigService(nacosConfigProperties.assembleConfigServiceProperties());
        // 程序首次启动, 并加载初始动态定时任务的配置
        String initConfigInfo = configService.getConfig(dataId, nacosConfigProperties.getGroup(), 5000);
        // 把配置文件解析成key value 的模式

        System.out.println("parament:"+initConfigInfo);
    }
}
