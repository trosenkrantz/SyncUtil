package com.github.trosenkrantz.sync.util.concurrency;

/**
 * A synchronous task ready to be run.
 */
public interface SynchronousTask extends Runnable {
    /**
     * Runs this task.
     * this returns or throws an {@link Throwable}, the task is considered done.
     */
    @Override
    void run();
}
