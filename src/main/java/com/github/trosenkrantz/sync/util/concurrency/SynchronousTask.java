package com.github.trosenkrantz.sync.util.concurrency;

/**
 * An synchronous task ready to be run.
 */
public interface SynchronousTask {
    /**
     * Runs this task.
     */
    void run();
}
