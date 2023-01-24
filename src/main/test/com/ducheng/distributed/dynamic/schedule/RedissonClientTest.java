package com.ducheng.distributed.dynamic.schedule;

import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = SchedulingApplication.class,webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class RedissonClientTest {

    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void getLock() {
        RLock lock = redissonClient.getLock("xxxxx");
        System.out.printf("xxxxx:"+ lock);
    }
}
