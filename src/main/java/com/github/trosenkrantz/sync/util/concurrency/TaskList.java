package com.github.trosenkrantz.sync.util.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * A collection of tasks ordered by when they were added.
 * This can contain a mix of {@link AsynchronousTask} and {@link SynchronousTask} objects.
 * You can use this to queue multiple tasks at once with {@link ConcurrentTaskDriver#queue(TaskList)}.
 * You can reuse the task list by queueing it again.
 */
public class TaskList {
    private final List<Task> tasks;

    /**
     * Creates with no initial tasks.
     */
    public TaskList() {
        tasks = new ArrayList<>();
    }

    /**
     * Creates with specified asynchronous tasks.
     * @param tasks initial tasks.
     */
    public TaskList(final AsynchronousTask... tasks) {
        this();
        add(tasks);
    }

    /**
     * Creates with specified synchronous tasks.
     * @param tasks initial tasks
     */
    public TaskList(final SynchronousTask... tasks) {
        this();
        add(tasks);
    }

    /**
     * Adds asynchronous tasks.
     * @param tasks tasks to add
     */
    public void add(final AsynchronousTask... tasks) {
        Stream.of(tasks).forEach(task -> this.tasks.add(new Task(task)));
    }

    /**
     * Adds synchronous tasks.
     * @param tasks tasks to add
     */
    public void add(final SynchronousTask... tasks) {
        Stream.of(tasks).forEach(task -> this.tasks.add(new Task(task)));
    }

    public List<Task> getTasks() {
        return tasks;
    }
}
