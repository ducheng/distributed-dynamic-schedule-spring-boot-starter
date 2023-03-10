package com.ducheng.distributed.dynamic.schedule;

import com.ducheng.distributed.dynamic.schedule.annotation.EnableDistributedDynamicScheduling;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *  启动类，用于做单元测试使用的
 */
@SpringBootApplication
@EnableDistributedDynamicScheduling
public class SchedulingApplication {
    public static void main(String[] args) {
        SpringApplication.run(SchedulingApplication.class);
    }
}
