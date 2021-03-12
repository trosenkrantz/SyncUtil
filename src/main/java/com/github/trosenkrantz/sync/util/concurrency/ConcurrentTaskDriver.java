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
    private final Queue<TaskHandler> queue = new ArrayDeque<>();

    private volatile int tasksStarted = 0;
    private volatile int tasksFinished = 0;
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
     * Queues an asynchronous task.
     * @param task the task to queue
     */
    public void queue(final AsynchronousTask task) {
        synchronized (this) {
            queue.add(toTaskHandler(task));
        }

        updateTasks();
    }

    /**
     * Queues a synchronous task.
     * @param task task to queue
     */
    public void queue(final SynchronousTask task) {
        synchronized (this) {
            queue.add(toTaskHandler(task));
        }

        updateTasks();
    }

    /**
     * Queues a list of tasks.
     * Queues them in the order they were added to the specified object.
     * @param tasks the tasks to queue
     */
    public void queue(final TaskList tasks) {
        synchronized (this) {
            queue.addAll(tasks.getTasks().stream().map(taskContainer ->
                    taskContainer.apply(this::toTaskHandler, this::toTaskHandler)
            ).collect(Collectors.toList()));
        }

        updateTasks();
    }

    private TaskHandler toTaskHandler(final AsynchronousTask task) {
        return () -> task.run(new SingleRunnable(ConcurrentTaskDriver.this::onTaskDone));
    }

    private TaskHandler toTaskHandler(final SynchronousTask task) {
        return () -> {
            try {
                task.run();
            } finally {
                onTaskDone();
            }
        };
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
        Optional<TaskHandler> nextTask;
        while ((nextTask = getNextTask()).isPresent()) {
            nextTask.get().startTask();
        }

        notifyListeners();
    }

    private void notifyListeners() {
        int numberOfQueuedTasks;
        int numberOfRunningTasks;
        int numberOfFinishedTasks;
        List<ConcurrentTasksListener> listenersCopy;

        synchronized (this) {
            if (listeners.isEmpty()) return;

            numberOfQueuedTasks = getNumberOfQueuedTasks();
            numberOfRunningTasks = getNumberOfRunningTasks();
            numberOfFinishedTasks = getNumberOfFinishedTasks();

            // Copy listeners to allow them to remove themselves while being notified
            listenersCopy = new ArrayList<>(listeners);
        }

        listenersCopy.forEach(listener -> listener.onProgress(numberOfQueuedTasks, numberOfRunningTasks, numberOfFinishedTasks));
    }

    private synchronized Optional<TaskHandler> getNextTask() {
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
