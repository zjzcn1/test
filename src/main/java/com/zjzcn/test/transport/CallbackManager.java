package com.zjzcn.test.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CallbackManager {

    private static Logger logger = LoggerFactory.getLogger(NettyClient.class);

    public static final int CLIENT_MAX_REQUEST = 2000;
    public static final int TIMEOUT_CHECK_MS = 100;

    // 回收过期任务
    private static ScheduledExecutorService requestTimeoutExecutor = Executors.newScheduledThreadPool(2);
    // 异步的request，需要注册callback future
    // 触发remove的操作有： 1) service的返回结果处理。 2) timeout thread cancel
    private ConcurrentMap<String, ResponseFuture> callbacks = new ConcurrentHashMap<>();
    private ScheduledFuture<?> requestTimeoutFuture = null;

    public CallbackManager() {
        requestTimeoutFuture = requestTimeoutExecutor.scheduleWithFixedDelay( new Runnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                for (String requestId : callbacks.keySet()) {
                    try {
                        ResponseFuture future = callbacks.get(requestId);
                        if (future.getCreateTime() + future.getTimeoutMillis() < currentTime) {
                            removeCallback(requestId);
                            future.cancel();
                        }
                    } catch (Exception e) {
                        logger.error("Client clear timeout future, requestId=" + requestId, e);
                    }
                }
            }
        }, TIMEOUT_CHECK_MS, TIMEOUT_CHECK_MS, TimeUnit.MILLISECONDS);
    }

    public void registerCallback(String requestId, ResponseFuture nettyResponseFuture) {
        if (callbacks.size() >= CLIENT_MAX_REQUEST) {
            throw new RuntimeException("Client over max concurrent request, drop request, requestId=" + requestId);
        }
        callbacks.put(requestId, nettyResponseFuture);
    }

    public ResponseFuture removeCallback(String requestId) {
        return callbacks.remove(requestId);
    }

    public void cancelTimeoutChecker() {
        requestTimeoutFuture.cancel(true);
    }

    public void clearCallback() {
        callbacks.clear();
    }
}
