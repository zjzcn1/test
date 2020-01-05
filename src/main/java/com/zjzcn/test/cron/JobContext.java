package com.zjzcn.test.cron;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JobContext {

    private String uuid;
    private Job job;
    private Map<String, Object> data = new HashMap<>();

    public JobContext(Job job) {
        this.job = job;
        this.uuid = UUID.randomUUID().toString();
    }

    public String getUuid() {
        return uuid;
    }

    public Job getJob() {
        return job;
    }

    public Object putData(String key, Object value) {
        return data.put(key, value);
    }

    public Object getData(String key) {
        return data.get(key);
    }

    public Map<String, Object> getDataMap() {
        return data;
    }
}
