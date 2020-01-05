package com.zjzcn.test.cron;

public class JobResult {

    private boolean success;
    private String message;

    private JobResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static JobResult success() {
        return new JobResult(true, null);
    }

    public static JobResult failure(String message) {
        return new JobResult(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
