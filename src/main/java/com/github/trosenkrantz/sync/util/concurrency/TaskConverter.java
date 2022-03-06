package com.github.trosenkrantz.sync.util.concurrency;

import com.github.trosenkrantz.sync.util.runnable.SingleRunnable;

import java.util.concurrent.CountDownLatch;

/**
 * Utility to convert between {@link AsynchronousTask} and {@link SynchronousTask}.
 * <p>
 * You can for instance use this, if you are handling mixed tasks;
 * instead of handling the two types differently, you can convert your synchronous tasks to asynchronous ones, so that you only need to implement handling for one type.
 */
public class TaskConverter {
    private TaskConverter() {
    }

    /**
     * Converts a synchronous task to an asynchronous one.
     * @param task the task to convert
     * @return the converted class
     */
    public static AsynchronousTask toAsynchronous(final SynchronousTask task) {
        return onDone -> {
            try {
                task.run();
            } finally {
                onDone.run();
            }
        };
    }

    /**
     * Converts an asynchronous task to a synchronous one.
     * The resulting task blocks the running thread until the onDone on the inner asynchronous task is called.
     * @param task the task to convert
     * @return the converted class
     */
    public static SynchronousTask toSynchronous(final AsynchronousTask task) {
        return () -> {
            try {
                CountDownLatch latch = new CountDownLatch(1);
                task.run(new SingleRunnable(latch::countDown));
                latch.await();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
    }
}
