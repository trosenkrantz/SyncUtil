package com.github.trosenkrantz.sync.util.concurrency;

import com.github.trosenkrantz.sync.util.runnable.SingleRunnable;

/**
 * An asynchronous task ready to be run.
 */
public interface AsynchronousTask {
    /**
     * Runs this task. The implementation must ensure {@code onDone} is called when the task is done, even when failed.
     * @param onDone runnable to run when this task is done
     */
    void run(final SingleRunnable onDone);
}
