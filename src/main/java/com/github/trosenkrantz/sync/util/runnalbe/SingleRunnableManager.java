package com.github.trosenkrantz.sync.util.runnalbe;

/**
 * A handler to only run once.
 * Here you at run-time specifies what runnable to run.
 */
public class SingleRunnableManager {
    private volatile RunnableState state;
    private Runnable scheduledRunnable;

    public SingleRunnableManager() {
        this(true);
    }

    public SingleRunnableManager(boolean allowed) {
        state = allowed ? RunnableState.IDLE : RunnableState.SUSPENDED;
    }

    /**
     * If this is the first call to this method in this instance, the runnable is run.
     * Otherwise, this method does nothing.
     *
     * @param runnable the runnable
     */
    public synchronized void run(final Runnable runnable) {
        synchronized (this) {
            switch (state) {
                case IDLE:
                    state = RunnableState.RUN;
                    break;
                case SUSPENDED:
                    scheduledRunnable = runnable;
                    state = RunnableState.SCHEDULED;
                    return;
                case SCHEDULED:
                case RUN:
                    return;
            }
        }

        runnable.run();
    }

    public void allow() {
        synchronized (this) {
            switch (state) {
                case IDLE:
                case RUN:
                    return;
                case SUSPENDED:
                    state = RunnableState.IDLE;
                    return;
                case SCHEDULED:
                    state = RunnableState.RUN;
                    break; // Break in order to run
            }
        }

        scheduledRunnable.run();
    }

    public synchronized void suspend() {
        switch (state) {
            case IDLE:
                state = RunnableState.SUSPENDED;
                break;
            case SUSPENDED:
            case RUN:
            case SCHEDULED:
                break;
        }
    }
}
