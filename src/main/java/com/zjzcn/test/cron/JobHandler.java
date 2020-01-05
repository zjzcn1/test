package com.zjzcn.test.cron;

public interface JobHandler {

    JobResult execute(JobContext context);

}
