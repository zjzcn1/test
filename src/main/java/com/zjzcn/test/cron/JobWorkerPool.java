package com.zjzcn.test.cron;

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class JobWorkerPool {

    private List<Thread> workers = new LinkedList<>();
    private int threadSize;
    private BlockingQueue<JobContext> queue;
    private JobListener listener;
    private AtomicBoolean stopped = new AtomicBoolean(false);

    public JobWorkerPool(int threadSize, int queueSize, JobListener listener) {
        this.threadSize = threadSize;
        queue = new ArrayBlockingQueue<>(queueSize);
        this.listener = listener;
    }

    public synchronized void start() {
        if (!stopped.get()) {
            return;
        }
        stopped.set(false);
        for (int i = 1; i <= threadSize; ++i) {
            Thread worker = new Thread("JobWorker-" + i) {
                @Override
                public void run() {
                    while (!stopped.get()) {
                        JobContext context = takeJob();
                        if (context != null && !context.getJob().isDeleted()) {
                            Job job = context.getJob();
                            try {
                                if (listener != null) {
                                    listener.onExecuted(context);
                                }

                                JobResult result;
                                if (job.getTimeoutSeconds() > 0) {
                                    FutureTask<JobResult> futureTask = new FutureTask<>(new Callable<JobResult>() {
                                        @Override
                                        public JobResult call() {
                                            return job.getJobHandler().execute(context);
                                        }
                                    });
                                    Thread futureThread = new Thread(futureTask);
                                    futureThread.start();

                                    result = futureTask.get(job.getTimeoutSeconds(), TimeUnit.SECONDS);
                                } else {
                                    result = job.getJobHandler().execute(context);
                                }

                                if (result != null) {
                                    if (listener != null) {
                                        if (result.isSuccess()) {
                                            listener.onSuccess(context);
                                        } else {
                                            listener.onFailure(context, result.getMessage());
                                        }
                                    }
                                }

                                job.getJobHandler().execute(context);
                                if (listener != null) {
                                    listener.onSuccess(context);
                                }
                            } catch (TimeoutException e) {
                                if (listener != null) {
                                    listener.onTimeout(context);
                                }
                            } catch (InterruptedException ignored) {
                            } catch (Throwable e) {
                                log.error("Error while executing job[{}].", job.getJobName(), e);
                                if (listener != null) {
                                    listener.onError(context, e);
                                }
                            }
                        }
                    }
                    log.info("{} is shutdown.", this.getName());
                }
            };
            workers.add(worker);
            worker.start();
            log.info("{} is started.", worker.getName());
        }
    }

    public void submitJob(JobContext context) {
        try {
            queue.put(context);
        } catch (InterruptedException ignored) {
        }
    }

    public JobContext takeJob() {
        try {
            return queue.take();
        } catch (InterruptedException ignored) {
        }
        return null;
    }

    public synchronized void shutdown() {
        if (stopped.get()) {
            return;
        }
        log.info("Shutting down job worker thread pool...");
        stopped.set(true);

        boolean interrupted = false;
        for (Thread worker : workers) {
            try {
                worker.interrupt();
                if (worker.getState() != Thread.State.TERMINATED) {
                    worker.join();
                }
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
        workers.clear();
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
        log.info("Job worker thread pool is shutdown.");
    }

}
