package com.github.trosenkrantz.sync.util.concurrency;

import com.github.trosenkrantz.sync.util.runnable.SingleRunnable;

import java.util.HashSet;
import java.util.Set;

/**
 * A tasks you that notifies you when done.
 * It has an inner task specified in the constructor.
 * When using this class, you should queue this instance and not the inner task.
 */
public class NotifyingTask implements AsynchronousTask {
    private final Set<Runnable> subscribers;
    protected final AsynchronousTask innerTask;

    private boolean hasFinished = false;

    /**
     * Constructs with an inner asynchronous task that this represents.
     * @param innerTask inner task
     */
    public NotifyingTask(final AsynchronousTask innerTask) {
        this.innerTask = innerTask;
        subscribers = new HashSet<>();
    }

    /**
     * Constructs with an inner asynchronous task that this represents.
     * @param innerTask inner task
     * @param subscriber subscriber to be notified whenever the task finishes
     */
    public NotifyingTask(final AsynchronousTask innerTask, final Runnable subscriber) {
        this(innerTask);
        subscribers.add(subscriber);
    }

    /**
     * Constructs with an inner synchronous task that this represents.
     * @param innerTask inner task
     */
    public NotifyingTask(final SynchronousTask innerTask) {
        this(TaskConverter.toAsynchronous(innerTask));
    }

    /**
     * Constructs with an inner synchronous task that this represents.
     * @param innerTask inner task
     * @param subscriber subscriber to be notified whenever the task finishes
     */
    public NotifyingTask(final SynchronousTask innerTask, final Runnable subscriber) {
        this(TaskConverter.toAsynchronous(innerTask), subscriber);
    }

    /**
     * Add a subscriber to be notified whenever the task finishes.
     * If the task has already finished at least once, this subscriber is notified immediately.
     * @param subscriber subscriber
     */
    public void subscribe(final Runnable subscriber) {
        boolean hasRunLocal;
        synchronized (this) { // Run in synchronized block to avoid race conditions with the onDone
            subscribers.add(subscriber);
            hasRunLocal = hasFinished;
        }

        if (hasRunLocal) subscriber.run(); // Run out of synchronous block, since we do not control what the subscriber does here
    }

    @Override
    public void run(final SingleRunnable onDone) {
        innerTask.run(new SingleRunnable(() -> {
            HashSet<Runnable> subscribersCopy; // Use a copy to avoid race conditions with subscribe
            synchronized (this) {
                subscribersCopy = new HashSet<>(subscribers);
                hasFinished = true;
            }
            subscribersCopy.forEach(Runnable::run); // Run out of synchronous block, since we do not control what the subscriber does here
            onDone.run();
        }));
    }
}
