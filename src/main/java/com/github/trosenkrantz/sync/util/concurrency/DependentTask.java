package com.github.trosenkrantz.sync.util.concurrency;

import com.github.trosenkrantz.sync.util.runnable.SingleRunnable;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A task that depends on other tasks to finish before this should run.
 */
public class DependentTask extends NotifyingTask {
    /**
     * Constructs with an inner asynchronous task that this represents.
     * @param innerTask inner task
     */
    public DependentTask(final AsynchronousTask innerTask) {
        super(innerTask);
    }

    /**
     * Constructs with an inner synchronous task that this represents.
     * @param innerTask inner task
     */
    public DependentTask(final SynchronousTask innerTask) {
        super(innerTask);
    }

    /**
     * Schedules this to be run at some point after some specified tasks are all finished.
     * @param driver         driver to queue at when all dependent tasks are finished
     * @param dependentTasks dependent tasks
     */
    public void schedule(final ConcurrentTaskDriver driver, final NotifyingTask... dependentTasks) {
        if (dependentTasks.length == 0) {
            driver.queue(this);
            return;
        }

        AtomicInteger countDown = new AtomicInteger(dependentTasks.length);

        for (NotifyingTask task : dependentTasks) {
            task.subscribe(new SingleRunnable(() -> {
                if (countDown.decrementAndGet() == 0) driver.queue(this);
            }));
        }
    }
}
