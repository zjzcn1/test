package com.zjzcn.test.cron;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Job {

    private String jobName;
    private JobHandler jobHandler;
    private String cron;
    private CronExpression cronExpression;
    private int timeoutSeconds;
    private Map<String, Object> jobData = new HashMap<>();
    private Date nextTriggerTime;
    private boolean deleted;

    public Job(String jobName, String cron, JobHandler jobHandler) {
        this(jobName, cron, 0, jobHandler);
    }

    public Job(String jobName, String cron, int timeoutSeconds, JobHandler jobHandler) {
        this.jobName = jobName;
        this.cron = cron;
        this.timeoutSeconds = timeoutSeconds;
        this.jobHandler = jobHandler;

        try {
            this.cronExpression = new CronExpression(cron);
        } catch (ParseException e) {
            throw new IllegalArgumentException("cron=" + cron, e);
        }
        nextTriggerTime = cronExpression.getNextValidTimeAfter(new Date());
    }

    public Date getNextTimeAfter(Date time) {
        return cronExpression.getNextValidTimeAfter(time);
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public JobHandler getJobHandler() {
        return jobHandler;
    }

    public void setJobHandler(JobHandler jobHandler) {
        this.jobHandler = jobHandler;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public Map<String, Object> getJobData() {
        return jobData;
    }

    public void setJobData(Map<String, Object> jobData) {
        this.jobData = jobData;
    }

    public Date getNextTriggerTime() {
        return nextTriggerTime;
    }

    public void setNextTriggerTime(Date nextTriggerTime) {
        this.nextTriggerTime = nextTriggerTime;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
