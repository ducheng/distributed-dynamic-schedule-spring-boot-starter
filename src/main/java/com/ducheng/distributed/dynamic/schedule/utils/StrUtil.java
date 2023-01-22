package com.ducheng.distributed.dynamic.schedule.utils;

public class StrUtil {
    /**
     *  动态占位符 替换
     * @param valueResolver
     * @return
     */
    public static String  resolveKey(String valueResolver) {
        String replace = valueResolver.replace("${", "").replace("}", "");
        return  replace;
    }
}
