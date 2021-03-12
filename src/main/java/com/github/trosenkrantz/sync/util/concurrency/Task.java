package com.github.trosenkrantz.sync.util.concurrency;

import java.util.function.Function;

/**
 * Contains either a asynchronous or synchronous task.
 */
public class Task {
    private AsynchronousTask asynchronousTask;
    private SynchronousTask synchronousTask;

    /**
     * Creates an asynchronous task.
     * @param asynchronousTask the task
     */
    public Task(final AsynchronousTask asynchronousTask) {
        this.asynchronousTask = asynchronousTask;
    }

    /**
     * Creates a synchronous task.
     * @param synchronousTask the task
     */
    public Task(final SynchronousTask synchronousTask) {
        this.synchronousTask = synchronousTask;
    }

    /**
     * Apply a function to the contained task.
     * @param functionForAsynchronous function to use if this contains an asynchronous task
     * @param functionForSynchronous  function to use if this contains a synchronous task
     * @param <T>                     the type of the result of the functions
     * @return the result of the corresponding function
     */
    public <T> T apply(final Function<? super AsynchronousTask, ? extends T> functionForAsynchronous, final Function<? super SynchronousTask, ? extends T> functionForSynchronous) {
        return asynchronousTask != null ? functionForAsynchronous.apply(asynchronousTask) : functionForSynchronous.apply(synchronousTask);
    }
}
