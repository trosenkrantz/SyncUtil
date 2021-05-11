package com.github.trosenkrantz.sync.util.concurrency;

import com.github.trosenkrantz.sync.util.runnable.SingleRunnable;

import java.util.concurrent.ExecutorService;

/**
 * A task that runs within an {@link ExecutorService}.
 */
public class ExecutorServiceTask implements AsynchronousTask {
    private final SynchronousTask task;
    private final ExecutorService executorService;

    /**
     * Constructs this.
     * When someone runs the returned outer task, the specified executor service will execute the inner task at some point.
     * @param task            inner task
     * @param executorService executor service
     */
    public ExecutorServiceTask(final SynchronousTask task, final ExecutorService executorService) {
        this.task = task;
        this.executorService = executorService;
    }

    @Override
    public void run(final SingleRunnable onDone) {
        executorService.execute(() -> {
            try {
                task.run();
            } finally {
                onDone.run();
            }
        });
    }
}
