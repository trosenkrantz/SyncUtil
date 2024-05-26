package com.github.trosenkrantz.sync.util.runnable;

// Overloaded class to add a new, unexpected state
enum RunnableState {
    IDLE,
    SUSPENDED,
    SCHEDULED,
    RUN,
    NEW_VALUE
}
