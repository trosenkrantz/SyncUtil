package com.github.trosenkrantz.sync.util.concurrency;

/**
 * An synchronous task ready to be run.
 */
public interface SynchronousTask extends Runnable {
    /**
     * Runs this task.
     */
    @Override
    void run();
}
