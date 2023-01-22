package com.ducheng.distributed.dynamic.schedule.test;

import com.ducheng.distributed.dynamic.schedule.annotation.DynamicScheduled;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class DemoTest {

    private static Log log = LogFactory.getLog(DemoTest.class);

    @DynamicScheduled(cron = "${test.cron}",desc = "test")
    public void test1() {

        log.info("testPrintlydate"+new Date());
    }
}
