package com.github.trosenkrantz.sync.util.concurrency;

/**
 * Listener to be notified when changes are made to which tasks are running.
 */
public interface ConcurrentTasksListener {

    /**
     * Called, when a task has been added or completed.
     * You may remove this listener with {@link ConcurrentTaskDriver#removeListener(ConcurrentTasksListener)} in this method.
     * @param queued   number of queued tasks
     * @param running  number of running tasks
     * @param finished number of finished tasks
     */
    void onProgress(final int queued, final int running, final int finished);
}
