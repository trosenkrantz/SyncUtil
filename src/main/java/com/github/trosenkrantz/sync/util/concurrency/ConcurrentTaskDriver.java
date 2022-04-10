package com.github.trosenkrantz.sync.util.concurrency;

import com.github.trosenkrantz.sync.util.runnable.SingleRunnable;

import java.util.*;

/**
 * Driver for managing tasks that run concurrently.
 * You can queue tasks to the driver at any time.
 * <p>
 * This driver does not use threading.
 * If you want to run tasks with high CPU usage, you should handle threading yourself.
 * You could do that by wrapping your tasks in {@link ExecutorServiceTask}.
 */
public class ConcurrentTaskDriver {
    private final List<ConcurrentTasksListener> listeners = new ArrayList<>();
    private final Queue<AsynchronousTask> queue = new ArrayDeque<>();
    private Limit maxRunningTasks;

    private volatile int tasksStarted = 0;
    private volatile int tasksFinished = 0;
    private volatile boolean suspended = false;

    /**
     * Constructs with no limit to number of running tasks.
     */
    public ConcurrentTaskDriver() {
        this.maxRunningTasks = Limit.noLimit();
    }

    /**
     * Constructs with a specific limit to number of running tasks.
     * @param maxRunningTasks maximum number of tasks that can run simultaneously
     */
    public ConcurrentTaskDriver(final int maxRunningTasks) {
        this.maxRunningTasks = Limit.of(maxRunningTasks);
    }

    public synchronized void setMaxRunningTasks(final Limit maxRunningTasks) {
        synchronized (this) {
            this.maxRunningTasks = maxRunningTasks;
        }
        updateTasks();
    }

    /**
     * Queues one of more asynchronous tasks.
     * @param tasks the tasks to queue
     */
    public void queue(final AsynchronousTask... tasks) {
        synchronized (this) {
            Collections.addAll(queue, tasks);
        }

        updateTasks();
    }

    /**
     * Queues one or more synchronous tasks.
     * @param task task to queue
     */
    public void queue(final SynchronousTask... task) {
        queue(Arrays.stream(task).map(TaskConverter::toAsynchronous).toArray(AsynchronousTask[]::new)); // Treat as asynchronous to only handle one type of tasks
    }

    private void startTask(final AsynchronousTask task) {
        task.run(new SingleRunnable(this::onTaskDone));
    }

    /**
     * Clears the queue of tasks not yet started.
     * Already running tasks are unaffected.
     */
    public void clearQueue() {
        synchronized (this) {
            queue.clear();
        }
        notifyListeners();
    }

    private void onTaskDone() {
        synchronized (this) {
            tasksFinished++;
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
        return tasksStarted - tasksFinished;
    }

    public synchronized int getNumberOfFinishedTasks() {
        return tasksFinished;
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
     * @param whenIdle called when no more tasks are running, or immediately if no tasks are currently running
     */
    public void suspend(final Runnable whenIdle) {
        boolean isAlreadyIdle = false;
        synchronized (this) {
            suspend();
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

        // Run the Runnable outside the synchronized block
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
        Optional<AsynchronousTask> nextTask;
        while ((nextTask = getNextTask()).isPresent()) {
            startTask(nextTask.get());
        }

        notifyListeners();
    }

    private void notifyListeners() {
        // Copy listeners to allow modification while iterating, e.g., allow listeners to remove themselves while being notified
        List<ConcurrentTasksListener> listenersCopy = new ArrayList<>(listeners);
        if (listenersCopy.isEmpty()) return;

        int numberOfQueuedTasks = getNumberOfQueuedTasks();
        int numberOfRunningTasks = getNumberOfRunningTasks();
        int numberOfFinishedTasks = getNumberOfFinishedTasks();

        listenersCopy.forEach(listener -> listener.onProgress(numberOfQueuedTasks, numberOfRunningTasks, numberOfFinishedTasks));
    }

    private synchronized Optional<AsynchronousTask> getNextTask() {
        if (shouldStartNewTask()) {
            tasksStarted++;
            return Optional.of(queue.remove());
        } else {
            return Optional.empty();
        }
    }

    private synchronized boolean shouldStartNewTask() {
        return maxRunningTasks.isGreaterThan(getNumberOfRunningTasks()) && !queue.isEmpty() && !isSuspended();
    }
}
