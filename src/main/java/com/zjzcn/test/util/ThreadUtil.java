package com.zjzcn.test.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadUtil {

    private static final Logger logger = LoggerFactory.getLogger(ThreadUtil.class);

    private static final String TAG = "ThreadUtil";

    private static ExecutorService executorService = Executors.newFixedThreadPool(5);

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            logger.warn("Thread.sleep InterruptedException", e);
        }
    }

    public static void execute(Runnable runnable) {
        executorService.execute(runnable);
    }
}
