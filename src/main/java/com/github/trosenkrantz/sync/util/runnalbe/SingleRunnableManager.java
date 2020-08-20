package com.github.trosenkrantz.sync.util.runnalbe;

/**
 * A handler to only run once.
 * You specify at run-time what runnable to run.
 * <p>
 * You can suspend this.
 * If you call {@link #run(Runnable)} while suspended, it will be scheduled to run once {@link #allow()} is called.
 * Only the first scheduled {@link Runnable} is run.
 */
public class SingleRunnableManager {
    private volatile RunnableState state;
    private Runnable scheduledRunnable;

    /**
     * Constructs this and sets it to not suspended.
     */
    public SingleRunnableManager() {
        this(true);
    }

    /**
     * Constructs this and specific if allowed or suspended.
     *
     * @param allowed true if allowed, false if suspended
     */
    public SingleRunnableManager(final boolean allowed) {
        state = allowed ? RunnableState.IDLE : RunnableState.SUSPENDED;
    }

    /**
     * If this is the first call to this method in this instance, the runnable is run or scheduled (if suspended).
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

    /**
     * Allows this to run a runnable.
     * If a {@link Runnable} was scheduled, runs it.
     */
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

    /**
     * Suspends this, not allowing {@link Runnable}'s to run until {@link #allow()} is called.
     */
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
