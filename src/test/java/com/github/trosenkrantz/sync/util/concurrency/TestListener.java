package com.github.trosenkrantz.sync.util.concurrency;

/**
 * Simple implementation of {@link AsyncConcurrentTasksListener} that stores latest progress.
 */
public class TestListener implements AsyncConcurrentTasksListener {
    private int queued;
    private int running;
    private int finished;

    @Override
    public void onProgress(final int queued, final int running, final int finished) {
        this.queued = queued;
        this.running = running;
        this.finished = finished;
    }

    public int getQueued() {
        return queued;
    }

    public int getRunning() {
        return running;
    }

    public int getFinished() {
        return finished;
    }
}
