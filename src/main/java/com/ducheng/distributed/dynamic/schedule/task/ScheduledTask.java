package com.ducheng.distributed.dynamic.schedule.task;

import java.util.concurrent.ScheduledFuture;


public class ScheduledTask {

    volatile ScheduledFuture<?> future;

    /**
     * 取消定时任务
     */
    public void cancel() {
        ScheduledFuture<?> future = this.future;
        if (future == null) return;
        future.cancel(true);
    }


    /**
     * 判断是否已经取消
     * @return
     */
    public boolean isCancelled() {
        ScheduledFuture<?> future = this.future;
        if (future == null) return true;
        return future.isCancelled();
    }

}
