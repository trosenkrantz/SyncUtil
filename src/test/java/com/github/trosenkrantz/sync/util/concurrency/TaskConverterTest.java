package com.github.trosenkrantz.sync.util.concurrency;

import com.github.trosenkrantz.sync.util.runnable.SingleRunnable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.atomic.AtomicBoolean;

class TaskConverterTest {
    @Test
    void asyncToSyncCallDoneImmediately() {
        // Arrange
        AtomicBoolean isInnerRun = new AtomicBoolean(false);
        AsynchronousTask input = onDone -> {
            isInnerRun.set(true);
            onDone.run();
        };
        SynchronousTask output = TaskConverter.toSynchronous(input);

        // Act
        output.run(); // Run outer task

        // Assert
        Assertions.assertTrue(isInnerRun.get());
    }

    @Test
    @Timeout(1)
    void asyncToSyncInterrupt() {
        // Arrange
        AtomicBoolean isInnerRun = new AtomicBoolean(false);
        AsynchronousTask input = onDone -> {
            isInnerRun.set(true);
            Thread.currentThread().interrupt();
        };
        SynchronousTask output = TaskConverter.toSynchronous(input);

        // Act
        output.run(); // Run outer task

        // Assert
        Assertions.assertTrue(isInnerRun.get());
    }

    @Test
    void syncToAsync() {
        // Arrange
        AtomicBoolean isInnerRun = new AtomicBoolean(false);
        AtomicBoolean isOuterDone = new AtomicBoolean(false);
        SynchronousTask input = () -> isInnerRun.set(true);
        AsynchronousTask output = TaskConverter.toAsynchronous(input);

        // Act
        output.run(new SingleRunnable(() -> isOuterDone.set(true))); // Run outer task

        // Assert
        Assertions.assertTrue(isInnerRun.get());
        Assertions.assertTrue(isOuterDone.get());
    }
}