package com.zjzcn.test.cron;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class JobScheduler {

    private static final int DEFAULT_WORKER_POOL_SIZE = 10;
    private static final int DEFAULT_WORKER_QUEUE_SIZE = 10000;

    private JobWorkerPool workerPool;
    private Thread schedulerThread;
    private JobStore jobStore;
    private JobListener listener;
    private AtomicBoolean stopped;


    public static JobScheduler newScheduler() {
        return new JobScheduler(DEFAULT_WORKER_POOL_SIZE, DEFAULT_WORKER_QUEUE_SIZE, null);
    }

    public static JobScheduler newScheduler(JobListener listener) {
        return new JobScheduler(DEFAULT_WORKER_POOL_SIZE, DEFAULT_WORKER_QUEUE_SIZE, listener);
    }

    public static JobScheduler newScheduler(int workerThreadSize, int workerQueueSize, JobListener listener) {
        return new JobScheduler(workerThreadSize, workerQueueSize, listener);
    }

    private JobScheduler(int workerThreadSize, int workerQueueSize, JobListener listener) {
        this.listener = listener;
        jobStore = new JobStore();
        workerPool = new JobWorkerPool(workerThreadSize, workerQueueSize, listener);
    }

    public void start() {
        if (isStarted()) {
            return;
        }

        stopped = new AtomicBoolean(false);
        workerPool.start();

        schedulerThread = new Thread("JobScheduler") {
            @Override
            public void run() {
                while (!stopped.get()) {
                    long start = System.currentTimeMillis();
                    try {
                        List<Job> triggeredJobs = jobStore.getTriggeredJobs();
                        for (Job job : triggeredJobs) {
                            job.setNextTriggerTime(job.getNextTimeAfter(new Date()));

                            JobContext context = new JobContext(job);
                            if (listener != null) {
                                listener.onTriggered(context);
                            }
                            // submit job to queue
                            workerPool.submitJob(context);
                        }
                    } catch (Throwable e) {
                        log.error("Runtime error occurred in main trigger firing loop.", e);
                    } finally {
                        long cost = System.currentTimeMillis() - start;
                        if (cost < 1000) {
                            try {
                                Thread.sleep(1000 - System.currentTimeMillis() % 1000);
                            } catch (InterruptedException ignored) {
                            }
                        }
                    }
                }
            }
        };
        schedulerThread.start();
    }

    public boolean isStarted() {
        return stopped != null && !stopped.get();
    }

    public void shutdown() {
        stopped.set(true);
        try {
            schedulerThread.join();
        } catch (InterruptedException ignored) {
        }
        workerPool.shutdown();
    }

    public boolean isShutdown() {
        return stopped.get();
    }

    public void addJob(Job job) {
        jobStore.addJob(job);
        log.info("Job[{}] add in scheduler, triggeredAt={}", job.getJobName(),
                DateFormatUtils.format(job.getNextTriggerTime(), "yyyy-MM-dd HH:mm:ss"));
    }

    public boolean containJob(String jobName) {
        return jobStore.containJob(jobName);
    }

    public boolean removeJob(String jobName) {
        Job job = jobStore.removeJob(jobName);
        if (job != null) {
            job.setDeleted(true);
            return true;
        } else {
            return false;
        }
    }

    public static void main(String[] args) {
        JobScheduler scheduler = JobScheduler.newScheduler(new JobListener() {
            @Override
            public void onTriggered(JobContext context) {
                log.info("onTriggered:{}", context.getUuid());
            }

            @Override
            public void onExecuted(JobContext context) {
                log.info("onExecuted:{}", context.getUuid());
            }

            @Override
            public void onSuccess(JobContext context) {
                log.info("onSuccess:{}", context.getUuid());
            }

            @Override
            public void onFailure(JobContext context, String message) {
                log.info("onFailure:{}", context.getUuid());
            }

            @Override
            public void onTimeout(JobContext context) {
                log.info("onTimeout:{}", context.getUuid());
            }

            @Override
            public void onError(JobContext context, Throwable e) {
                log.info("onError:{}", context.getUuid());
            }
        });
        for (int i=0; i< 10; i++) {
            Job job = new Job("job" + i, "0 0/1 * * * ?", 9, (c) -> {
                log.info("name={}, next={}", c.getJob().getJobName(),
                        DateFormatUtils.format(c.getJob().getNextTriggerTime(), "HH:mm:ss"));
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("name={}", c.getJob().getJobName());
                return JobResult.success();
            });
            scheduler.addJob(job);
            scheduler.start();
        }

        for (int i=0; i< 10; i++) {
            Job job = new Job("job" + i + 10, "0 0/1 * * * ?", 11, (c) -> {
                log.info("name={}, next={}", c.getJob().getJobName(),
                        DateFormatUtils.format(c.getJob().getNextTriggerTime(), "HH:mm:ss"));
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("name={}", c.getJob().getJobName());
                return JobResult.success();
            });
            scheduler.addJob(job);
            scheduler.start();
        }

        scheduler.shutdown();

        scheduler.start();
    }
}
