package org.elasticsearch.contrib.plugin.helper;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

public class RetryTask {

    private static final ESLogger LOGGER = Loggers.getLogger(RetryTask.class);

    private int maximumAttempts;
    private long retryIntervalInMillis;
    private Class<? extends Exception>[] retryableExceptions;

    public RetryTask(int maximumAttempts, long retryIntervalInMillis) {
        this.maximumAttempts = maximumAttempts;
        this.retryIntervalInMillis = retryIntervalInMillis;
    }

    public RetryTask onExceptions(Class<? extends Exception>... retryableExceptions) {
        this.retryableExceptions = retryableExceptions;

        return this;
    }

    public <T> T execute(Task<T> task) {
        int currentAttempt = 0;

        while (currentAttempt < maximumAttempts) {
            currentAttempt++;

            LOGGER.info(String.format("Attempt[%d] for Task[%s]", currentAttempt, task.getClass().getName()));

            try {
                T result = task.execute();
                LOGGER.debug(String.format("Task result : %s", result));
                if (result != null) return result;
            } catch (RuntimeException e) {
                if (!isRetryableException(e)) {
                    LOGGER.info("Attempt failed with exception", e);
                    throw e;
                }
            }

            try {
                Thread.sleep(retryIntervalInMillis);
            } catch (InterruptedException e) {
                LOGGER.error("All retry attempts failed with exception", e);
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private boolean isRetryableException(RuntimeException e) {
        if (retryableExceptions == null || retryableExceptions.length == 0) return false;

        for (Object o : ExceptionUtils.getThrowableList(e)) {
            for (Class<? extends Exception> retryable : retryableExceptions) {
                if (retryable.isInstance(o)) {
                    return true;
                }
            }
        }

        return false;
    }

}