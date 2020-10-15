package com.github.trosenkrantz.sync.util.concurrency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final List<Runnable> queue = new ArrayList<>();

    private volatile int tasksStarted;
    private volatile int tasksEnded;
    private volatile Integer maxRunningTasks; // Null means no limit

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
     * Stops starting new tasks.
     * Currently running tasks are not affected.
     */
    public void cancelQueuedTasks() {
        synchronized (this) {
            queue.clear();
        }
        notifyListeners();
    }

    /**
     * Queues tasks.
     * Call {@link #onAsynchronousTaskDone()} whenever each task is done.
     * @param tasks the tasks to queue
     */
    public void queueAsynchronousTasks(final Collection<Runnable> tasks) {
        synchronized (this) {
            this.queue.addAll(tasks);
        }

        updateTasks();
    }

    /**
     * Queues a task.
     * When the task is done, be sure to call {@link #onAsynchronousTaskDone()}.
     * @param task the task to queue
     */
    public void queueAsynchronousTask(final Runnable task) {
        queueAsynchronousTasks(Stream.of(task).collect(Collectors.toList()));
    }

    /**
     * Should be called when a task attempt is done, even when failed.
     */
    public void onAsynchronousTaskDone() {
        synchronized (this) {
            tasksEnded++;
        }
        updateTasks();
    }

    /**
     * Queues tasks.
     * When the {@link Runnable#run()} of a task provided in this method returns, the task is considered done.
     * Do not call {@link #onAsynchronousTaskDone()} for this task.
     * @param tasks task to queue
     */
    public void queueSynchronousTasks(final Collection<Runnable> tasks) {
        queueAsynchronousTasks(tasks.stream().map(task ->
                (Runnable) () -> {
                    // Act as an asynchronous task that finishes right when run returns
                    task.run();
                    onAsynchronousTaskDone();
                }
        ).collect(Collectors.toList()));
    }

    /**
     * Queues a task.
     * When the {@link Runnable#run()} of the task returns, the task is considered done.
     * Do not call {@link #onAsynchronousTaskDone()} for this task.
     * @param task task to queue
     */
    public void queueSynchronousTask(final Runnable task) {
        queueSynchronousTasks(Stream.of(task).collect(Collectors.toList()));
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
     * Updates what tasks to run.
     */
    private void updateTasks() {
        while (allowedToStartTask() && !queue.isEmpty()) {
            Runnable task;
            synchronized (this) {
                task = queue.get(0);
                queue.remove(0);
                tasksStarted++;
            }
            task.run();
        }

        notifyListeners();
    }

    private void notifyListeners() {
        int numberOfQueuedTasks;
        int numberOfRunningTasks;
        int numberOfFinishedTasks;
        synchronized (this) {
            numberOfQueuedTasks = getNumberOfQueuedTasks();
            numberOfRunningTasks = getNumberOfRunningTasks();
            numberOfFinishedTasks = getNumberOfFinishedTasks();
        }
        listeners.forEach(listener -> listener.onProgress(numberOfQueuedTasks, numberOfRunningTasks, numberOfFinishedTasks));
    }

    private synchronized boolean allowedToStartTask() {
        return maxRunningTasks == null || getNumberOfRunningTasks() < maxRunningTasks;
    }
}