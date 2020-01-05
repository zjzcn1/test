package com.zjzcn.test.cron;

public interface JobListener {

    void onTriggered(JobContext context);

    void onExecuted(JobContext context);

    void onSuccess(JobContext context);

    void onFailure(JobContext context, String message);

    void onTimeout(JobContext context);

    void onError(JobContext context, Throwable e);

}
