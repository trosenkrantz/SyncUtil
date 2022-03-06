package com.github.trosenkrantz.sync.util.concurrency;

import com.github.trosenkrantz.sync.util.runnable.SingleRunnable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * A {@link TaskList} that notifies someone when all the tasks are done.
 */
public class NotifyingTaskList extends TaskList {
    private final Runnable notify;

    /**
     * Creates with no initial tasks.
     * @param notify to be called when all tasks are done
     */
    public NotifyingTaskList(final Runnable notify) {
        this.notify = notify;
    }

    /**
     * Creates with specified asynchronous tasks.
     * @param notify to be called when all tasks are done
     * @param tasks  initial tasks.
     */
    public NotifyingTaskList(final Runnable notify, final AsynchronousTask... tasks) {
        super(tasks);
        this.notify = notify;
    }

    /**
     * Creates with specified synchronous tasks.
     * @param notify to be called when all tasks are done
     * @param tasks  initial tasks
     */
    public NotifyingTaskList(final Runnable notify, final SynchronousTask... tasks) {
        super(tasks);
        this.notify = notify;
    }

    /**
     * Gets the tasks added so far.
     * When the tasks returned by this method are done, this instance will notify.
     * If you later on add tasks, it does not affect the object returned here and does not affect notify.
     * @return tasks added so far
     */
    @Override
    public List<AsynchronousTask> getTasks() {
        List<AsynchronousTask> tasks = super.getTasks();
        AtomicInteger tasksNotFinished = new AtomicInteger(tasks.size());
        return tasks.stream().map(task -> wrap(task, tasksNotFinished)).collect(Collectors.toList());
    }

    private AsynchronousTask wrap(final AsynchronousTask task, final AtomicInteger tasksNotFinished) {
        return onDone -> task.run(new SingleRunnable(() -> {
            if (tasksNotFinished.decrementAndGet() == 0) notify.run();
            onDone.run();
        }));
    }
}
