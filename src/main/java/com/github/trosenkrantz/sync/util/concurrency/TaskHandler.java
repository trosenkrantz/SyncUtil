package com.github.trosenkrantz.sync.util.concurrency;

/**
 * Used for storing what to do when starting a task in {@link ConcurrentTaskDriver}.
 */
public interface TaskHandler {
    /**
     * Runs the task represented by this.
     * When the task is done, notifies {@link ConcurrentTaskDriver}, e.g. so it can start new starts.
     */
    void startTask();
}
