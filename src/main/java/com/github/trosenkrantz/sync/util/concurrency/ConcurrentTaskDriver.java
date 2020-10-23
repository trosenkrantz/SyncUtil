package com.github.trosenkrantz.sync.util.concurrency;

import com.github.trosenkrantz.sync.util.runnable.SingleRunnable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Driver for managing and keeping track of tasks that run concurrently.
 * You can queue tasks to the driver at any time.
 * <p>
 * This driver does not use threading.
 * If you want to run tasks with high CPU usage, you should handle threading yourself.
 * You could do that by wrapping your tasks in {@link java.util.concurrent.ExecutorService#submit(Runnable)}.
 */
public class ConcurrentTaskDriver {
    private final List<ConcurrentTasksListener> listeners = new ArrayList<>();
    private final Queue<Runnable> queue = new ArrayDeque<>();

    private volatile int tasksStarted = 0;
    private volatile int tasksEnded = 0;
    private volatile Integer maxRunningTasks; // Null means no limit

    private volatile boolean suspended = false;

    /**
     * Constructs with no limit to number of running tasks.
     */
    public ConcurrentTaskDriver() {
        this(null);
    }

    /**
     * Constructs with a specific limit to number of running tasks.
     * @param maxRunningTasks maximum number of tasks that can run simultaneously
     */
    public ConcurrentTaskDriver(final Integer maxRunningTasks) {
        this.maxRunningTasks = maxRunningTasks;
    }

    public synchronized void setMaxRunningTasks(final Integer maxRunningTasks) {
        synchronized (this) {
            this.maxRunningTasks = maxRunningTasks;
        }
        updateTasks();
    }

    /**
     * Queues asynchronous task.
     * @param task the task to queue
     */
    public void queue(final AsynchronousTask task) {
        queueAsynchronous(Collections.singleton(task));
    }

    /**
     * Queues asynchronous tasks.
     * The tasks are queued in the order specified by the iterator of the specified collection.
     * @param tasks the tasks to queue
     */
    public void queueAsynchronous(final Collection<AsynchronousTask> tasks) {
        synchronized (this) {
            queue.addAll(tasks.stream().map(task ->
                    (Runnable) () -> task.run(new SingleRunnable(ConcurrentTaskDriver.this::onTaskDone))
            ).collect(Collectors.toList()));
        }

        updateTasks();
    }

    /**
     * Queues a synchronous task.
     * When the {@link SynchronousTask#run()} of the task returns or throws an exception, the task is considered done.
     * @param task task to queue
     */
    public void queue(final SynchronousTask task) {
        queueSynchronous(Collections.singleton(task));
    }

    /**
     * Queues synchronous tasks.
     * When the {@link SynchronousTask#run()} of a task provided here returns or throws an exception, the task is considered done.
     * The tasks are queued in the order specified by the iterator of the specified collection.
     * @param tasks the tasks to queue
     */
    public void queueSynchronous(final Collection<SynchronousTask> tasks) {
        synchronized (this) {
            queue.addAll(tasks.stream().map(task ->
                    (Runnable) () -> {
                        try {
                            task.run();
                        } finally {
                            onTaskDone();
                        }
                    }
            ).collect(Collectors.toList()));
        }

        updateTasks();
    }

    /**
     * Clears the queue of tasks not yet started.
     * Currently running tasks are unaffected.
     */
    public void clearQueue() {
        synchronized (this) {
            queue.clear();
        }
        notifyListeners();
    }

    private void onTaskDone() {
        synchronized (this) {
            tasksEnded++;
        }
        updateTasks();
    }

    public synchronized int getNumberOfQueuedTasks() {
        return queue.size();
    }

    /**
     * Gets the number of tasks currently running.
     * @return the number of tasks running
     */
    public synchronized int getNumberOfRunningTasks() {
        return tasksStarted - tasksEnded;
    }

    public synchronized int getNumberOfFinishedTasks() {
        return tasksEnded;
    }

    /**
     * Adds listener to be notified of updates.
     * @param listener listener to be notified
     */
    public void addListener(final ConcurrentTasksListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes listener added by {@link #addListener(ConcurrentTasksListener)}.
     * @param listener listener to remove
     */
    public void removeListener(final ConcurrentTasksListener listener) {
        listeners.remove(listener);
    }

    /**
     * Prevents starting new tasks until {@link #resume()} is called.
     */
    public synchronized void suspend() {
        suspended = true;
    }

    /**
     * Prevents starting new tasks until {@link #resume()} is called.
     * @param whenIdle called when no more tasks are running.
     */
    public void suspend(final Runnable whenIdle) {
        boolean isAlreadyIdle = false;
        synchronized (this) {
            suspended = true;
            if (getNumberOfRunningTasks() <= 0) {
                isAlreadyIdle = true;
            } else {
                // Wait for tasks to finish
                addListener(new ConcurrentTasksListener() {
                    @Override
                    public void onProgress(final int queued, final int running, final int finished) {
                        if (running <= 0) {
                            removeListener(this);
                            whenIdle.run();
                        }
                    }
                });
            }
        }

        if (isAlreadyIdle) whenIdle.run();
    }

    /**
     * Removes the prevention of starting new tasks caused by {@link #suspend()} or {@link #suspend(Runnable)}.
     */
    public void resume() {
        synchronized (this) {
            suspended = false;
        }
        updateTasks();
    }

    public boolean isSuspended() {
        return suspended;
    }

    /**
     * Updates what tasks to run, runs them and notifies listeners.
     */
    private void updateTasks() {
        Optional<Runnable> nextTask;
        while ((nextTask = getNextTask()).isPresent()) {
            nextTask.get().run();
        }

        notifyListeners();
    }

    private void notifyListeners() {
        int numberOfQueuedTasks;
        int numberOfRunningTasks;
        int numberOfFinishedTasks;
        List<ConcurrentTasksListener> listenersCopy;

        synchronized (this) {
            numberOfQueuedTasks = getNumberOfQueuedTasks();
            numberOfRunningTasks = getNumberOfRunningTasks();
            numberOfFinishedTasks = getNumberOfFinishedTasks();

            // Copy listeners to allow them to remove themselves while being notified
            listenersCopy = new ArrayList<>(listeners);
        }

        listenersCopy.forEach(listener -> listener.onProgress(numberOfQueuedTasks, numberOfRunningTasks, numberOfFinishedTasks));
    }

    private synchronized Optional<Runnable> getNextTask() {
        if (shouldStartNewTask()) {
            tasksStarted++;
            return Optional.of(queue.remove());
        } else {
            return Optional.empty();
        }
    }

    private synchronized boolean shouldStartNewTask() {
        return (maxRunningTasks == null || getNumberOfRunningTasks() < maxRunningTasks) && !queue.isEmpty() && !isSuspended();
    }
}
