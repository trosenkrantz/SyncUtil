package com.github.trosenkrantz.sync.util.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A collection of tasks ordered by when they were added.
 * This can contain a mix of {@link AsynchronousTask} and {@link SynchronousTask} objects.
 * You can use this to queue multiple tasks at once with {@link ConcurrentTaskDriver#queue(TaskList)}.
 * You can reuse a {@link TaskList} by queueing it again.
 * You can be notified when a list of tasks are done by using {@link NotifyingTaskList}.
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
    public final void add(final AsynchronousTask... tasks) {
        this.tasks.addAll(Stream.of(tasks).map(Task::new).collect(Collectors.toList()));
    }

    /**
     * Adds synchronous tasks.
     * @param tasks tasks to add
     */
    public final void add(final SynchronousTask... tasks) {
        this.tasks.addAll(Stream.of(tasks).map(Task::new).collect(Collectors.toList()));
    }

    public List<Task> getTasks() {
        return new ArrayList<>(tasks);
    }
}
