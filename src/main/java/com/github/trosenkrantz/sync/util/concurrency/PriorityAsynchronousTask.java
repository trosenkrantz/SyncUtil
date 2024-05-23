package com.github.trosenkrantz.sync.util.concurrency;

/**
 * Wrapper of an {@link AsynchronousTask} and a priority.
 */
public class PriorityAsynchronousTask implements Comparable<PriorityAsynchronousTask>{
    private final AsynchronousTask task;
    private final int priority;

    /**
     * Constructs this.
     * @param task task
     * @param priority priority, where lower values have higher priority
     */
    public PriorityAsynchronousTask(final AsynchronousTask task, final int priority) {
        this.task = task;
        this.priority = priority;
    }

    public AsynchronousTask getTask() {
        return task;
    }

    @Override
    public int compareTo(final PriorityAsynchronousTask other) {
        return Integer.compare(this.priority, other.priority);
    }
}
