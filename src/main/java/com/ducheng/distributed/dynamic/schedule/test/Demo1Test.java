package com.ducheng.distributed.dynamic.schedule.test;

import com.ducheng.distributed.dynamic.schedule.annotation.DynamicScheduled;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class Demo1Test {

    private static Log log = LogFactory.getLog(Demo1Test.class);

    @DynamicScheduled(cron = "${test1.cron.test}",desc = "test1")
    public void test1() {

        log.info("test1Printly"+new Date());
    }
}
