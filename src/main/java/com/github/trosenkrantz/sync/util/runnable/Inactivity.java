package com.github.trosenkrantz.sync.util.runnable;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Manages an inactivity timer that executes a runnable if no activity is signalled within a specified duration.
 * <p>
 * This class is thread-safe and allows for resetting the timer through activity markers.
 */
public class Inactivity {
    private final ScheduledExecutorService executor;

    private final Runnable inactivityRunnable;
    private final long inactivityPeriod;
    private final TimeUnit inactivityUnit;

    private volatile boolean active = true;

    private ScheduledFuture<?> inactivitySchedule;

    /**
     * Constructs an Inactivity tracker.
     *
     * @param executor           the executor service used to schedule the inactivity task
     * @param inactivityRunnable the task to run when the inactivity threshold is reached
     * @param inactivityPeriod   the duration to wait for activity before triggering the task
     * @param inactivityUnit     the time unit for the inactivity period
     */
    public Inactivity(ScheduledExecutorService executor, Runnable inactivityRunnable, long inactivityPeriod, TimeUnit inactivityUnit) {
        this.executor = executor;
        this.inactivityRunnable = inactivityRunnable;
        this.inactivityPeriod = inactivityPeriod;
        this.inactivityUnit = inactivityUnit;
    }

    /**
     * Starts the inactivity timer.
     */
    public synchronized void schedule() {
        markActivity();
    }

    /**
     * Signals that activity has occurred.
     * If the tracker is active, this cancels the current pending future and schedules a new runnable with the original delay.
     */
    public synchronized void markActivity() {
        if (!active) return;

        cancel();
        inactivitySchedule = executor.schedule(inactivityRunnable, inactivityPeriod, inactivityUnit);
    }

    private synchronized void cancel() {
        if (inactivitySchedule != null) inactivitySchedule.cancel(false);
    }

    /**
     * Permanently stops the inactivity tracker.
     * Cancels any pending tasks and prevents future tasks from being scheduled.
     */
    public synchronized void close() {
        cancel();
        active = false;
    }
}
