package com.github.trosenkrantz.sync.util.concurrency;

import com.github.trosenkrantz.sync.util.runnable.SingleRunnable;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * An {@link AsynchronousTask} that repeats on inner task a specified number of times or until manually stopped.
 */
public class RepeatingAsynchronousTask implements AsynchronousTask {
    private final AsynchronousTask task;
    private final Limit limit;
    private final AtomicInteger count = new AtomicInteger(0);

    private SingleRunnable onOuterDone;

    private volatile boolean shouldStop = false;

    /**
     * Creates the task to repeat without a specified limit.
     * @param task inner task
     */
    public RepeatingAsynchronousTask(final AsynchronousTask task) {
        this.task = task;
        this.limit = Limit.noLimit();
    }

    /**
     * Creates the task to repeat for a specified number of times.
     * This task will repeat until the inner task is run {@code limit} times, or until manually stopped.
     * @param task  inner task
     * @param limit limit
     */
    public RepeatingAsynchronousTask(final AsynchronousTask task, final int limit) {
        this.task = task;
        this.limit = Limit.of(limit);
    }

    /**
     * Stop repeating the inner task.
     * When the inner task is done next time, this outer task will be done.
     */
    public void stop() {
        shouldStop = true;
    }

    @Override
    public void run(final SingleRunnable onDone) {
        onOuterDone = onDone;

        task.run(new SingleRunnable(this::onInnerDone));
    }

    private void onInnerDone() {
        if (shouldStop || !limit.isGreaterThan(count.incrementAndGet())) onOuterDone.run();
        else task.run(new SingleRunnable(this::onInnerDone));
    }
}
