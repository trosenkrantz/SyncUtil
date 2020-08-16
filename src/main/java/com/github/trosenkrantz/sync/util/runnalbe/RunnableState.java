package com.github.trosenkrantz.sync.util.runnalbe;

enum RunnableState {
    IDLE,

    /**
     * Running has not been attempted.
     * If trying to run, we will schedule it.
     */
    SUSPENDED,

    /**
     * Running has been attempted, but is currently suspended.
     * When allowed, we will run.
     */
    SCHEDULED,

    RUN
}
