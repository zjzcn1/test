package com.zjzcn.test.cron;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class JobStore {

    private volatile Map<String, Job> jobs = new ConcurrentHashMap<>();

    public List<Job> getAllJobs() {
        return new ArrayList<>(jobs.values());
    }

    public void addJob(Job job) {
        if (jobs.containsKey(job.getJobName())) {
            throw new IllegalArgumentException("Job already exists, name=" + job.getJobName());
        }

        jobs.put(job.getJobName(), job);
    }

    public boolean containJob(String jobName) {
        return jobs.containsKey(jobName);
    }

    public Job removeJob(String jobName) {
        return jobs.remove(jobName);
    }

    public List<Job> getTriggeredJobs() {
        List<Job> triggeredJobs = new ArrayList<>();
        for (Job job : jobs.values()) {
            if (System.currentTimeMillis() >= job.getNextTriggerTime().getTime()) {
                triggeredJobs.add(job);
            }
        }
        return triggeredJobs;
    }
}
