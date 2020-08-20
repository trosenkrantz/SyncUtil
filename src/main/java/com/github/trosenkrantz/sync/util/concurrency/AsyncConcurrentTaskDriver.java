package com.github.trosenkrantz.sync.util.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Driver for running tasks concurrently.
 * You can add tasks to the driver at any time.
 * <p>
 * This is intended for managing asynchronous tasks that have little CPU usage.
 * For instance, it does not use threading.
 * If you want to manage CPU heavy tasks, consider using {@link java.util.concurrent.ExecutorService}.
 */
public class AsyncConcurrentTaskDriver {
    private final List<AsyncConcurrentTasksListener> listeners = new ArrayList<>();
    private final List<Runnable> queue = new ArrayList<>();

    private volatile int tasksStarted;
    private volatile int tasksEnded;
    private volatile Integer maxRunningTasks;

    public AsyncConcurrentTaskDriver() {
        this(null);
    }

    public AsyncConcurrentTaskDriver(final Integer maxRunningTasks) {
        this.maxRunningTasks = maxRunningTasks;
    }

    public synchronized void setMaxRunningTasks(final Integer maxRunningTasks) {
        synchronized (this) {
            this.maxRunningTasks = maxRunningTasks;
        }
        updateTasks();
    }

    public void addListener(final AsyncConcurrentTasksListener listener) {
        listeners.add(listener);
    }

    public void removeListener(final AsyncConcurrentTasksListener listener) {
        listeners.remove(listener);
    }

    /**
     * Stops starting new tasks.
     * Currently running tasks are not affected.
     */
    public void cancelNewTasks() {
        synchronized (this) {
            queue.clear();
        }
        notifyListeners();
    }

    /**
     * Adds tasks.
     *
     * @param tasks the tasks to add
     */
    public void addTasks(final List<Runnable> tasks) {
        synchronized (this) {
            this.queue.addAll(tasks);
        }

        updateTasks();
    }

    /**
     * Adds a task.
     *
     * @param task the task to add
     */
    public void addTask(final Runnable task) {
        addTasks(Stream.of(task).collect(Collectors.toList()));
    }

    /**
     * Should be called when a task attempt is done, even when failed.
     */
    public void onTaskDone() {
        synchronized (this) {
            tasksEnded++;
        }
        updateTasks();
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

    public synchronized int getNumberOfQueuedTasks() {
        return queue.size();
    }

    /**
     * Gets the number of tasks currently running.
     *
     * @return the number of tasks running
     */
    public synchronized int getNumberOfRunningTasks() {
        return tasksStarted - tasksEnded;
    }

    public synchronized int getNumberOfFinishedTasks() {
        return tasksEnded;
    }
}
