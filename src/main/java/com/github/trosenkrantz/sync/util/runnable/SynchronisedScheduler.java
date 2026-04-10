package com.github.trosenkrantz.sync.util.runnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A handler to synchronise scheduled events.
 * It operates in windows that contain exactly one event (running a {@link Runnable}).
 * On an event, it rolls to a new window, cancelling remaining futures.
 * <p>
 * It can also track inactivity, triggering an event when we have not marked an activity for a certain duration.
 */
public class SynchronisedScheduler {
    private final ScheduledExecutorService executor;
    private final List<ScheduledFuture<?>> manualSchedules = new ArrayList<>();
    private final List<Inactivity> inactivities = new ArrayList<>();

    private SingleRunnableManager activeWindow = new SingleRunnableManager();

    /**
     * Constructs a new scheduler using the provided executor service.
     *
     * @param executor the executor service used to schedule tasks
     */
    public SynchronisedScheduler(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    /**
     * Schedules a runnable to run after a specific delay.
     * If it is run, it triggers a window transition, marking activity and cancelling other pending schedules.
     *
     * @param runnable the task to execute
     * @param period   the time delay before execution
     * @param unit     the time unit of the delay
     * @return a {@link ScheduledFuture} representing the pending execution
     */
    public synchronized ScheduledFuture<?> schedule(Runnable runnable, long period, TimeUnit unit) {
        SingleRunnableManager window = activeWindow;
        ScheduledFuture<?> thisFuture = executor.schedule(() -> window.run(() -> {
            synchronized (this) {
                markActivity();
                activeWindow = new SingleRunnableManager(); // transition to next window

                close(false); // Cancel futures to safe memory usage
            }

            runnable.run();
        }), period, unit);
        manualSchedules.add(thisFuture);
        return thisFuture;
    }

    /**
     * Schedules a runnable to run if no activity is marked within the specified duration.
     * The runnable is used only within the currently active window.
     *
     * @param runnable the task to execute on inactivity
     * @param period   the inactivity threshold duration
     * @param unit     the time unit for the threshold
     */
    public synchronized void scheduleOnInactivity(Runnable runnable, long period, TimeUnit unit) {
        Inactivity inactivity = new Inactivity(executor, activeWindow.wrap(runnable), period, unit);
        inactivities.add(inactivity);
        inactivity.schedule();
    }

    /**
     * Signals activity to all registered inactivity trackers, resetting their timers.
     */
    public synchronized void markActivity() {
        inactivities.forEach(Inactivity::markActivity);
    }

    /**
     * Manually transitions the scheduler to a new window.
     * Cancels existing futures and seals the current window against further executions.
     */
    public synchronized void nextWindow() {
        close(false);

        activeWindow.run(() -> {
            // NOP to close window
        });
        activeWindow = new SingleRunnableManager();
    }

    /**
     * Closes the scheduler and cancels all pending manual schedules and inactivity trackers.
     *
     * @param mayInterruptIfRunning {@code true} if the thread executing a runnable should be interrupted; otherwise, in-progress runnable objects are allowed to complete
     */
    public synchronized void close(boolean mayInterruptIfRunning) {
        manualSchedules.forEach(future -> future.cancel(mayInterruptIfRunning));
        inactivities.forEach(Inactivity::close);
    }
}
