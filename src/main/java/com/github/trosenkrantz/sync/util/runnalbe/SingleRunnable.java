package com.github.trosenkrantz.sync.util.runnalbe;

/**
 * An object that only runs a runnable a single time.
 * If calling {@link #run()} more than once, the subsequent calls does nothing.
 */
public class SingleRunnable extends SingleRunnableManager implements Runnable {
    private final Runnable runnable;

    /**
     * Constructs this.
     * @param runnable the runnable to run only once
     */
    public SingleRunnable(final Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void run() {
        run(runnable);
    }
}
