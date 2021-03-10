package com.github.trosenkrantz.sync.util.concurrency;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

class ConcurrentTaskDriverOtherTest extends ConcurrentTaskDriverTest {

    @Test
    void listener() {
        driver = new ConcurrentTaskDriver(1);
        TestListener listener = new TestListener();
        driver.addListener(listener);

        driver.queueAsynchronous(Arrays.asList(asynchronousTask, asynchronousTask));
        assertTasks(listener, 1, 1, 0);

        finishTask();
        assertTasks(listener, 0, 1, 1);

        driver.removeListener(listener);
        finishTask();
        assertTasks(listener, 0, 1, 1);
    }

    @Test
    void clearQueue() {
        driver = new ConcurrentTaskDriver(1);

        driver.queueAsynchronous(Arrays.asList(asynchronousTask, asynchronousTask));
        assertTasks(1, 1, 0);

        driver.clearQueue();
        assertTasks(0, 1, 0);

        finishTask();
        assertTasks(0, 0, 1);
    }

    @Test
    void queueSynchronousTask() {
        driver = new ConcurrentTaskDriver();

        driver.queue(
                () -> assertTasks(0, 1, 0)
        );
        assertTasks(0, 0, 1);
    }

    @Test
    void queueSynchronousTasksLimit1() {
        // Arrange
        driver = new ConcurrentTaskDriver(1);

        // Act
        driver.queueSynchronous(Arrays.asList(
                () -> assertTasks(1, 1, 0),
                () -> assertTasks(0, 1, 1)
        ));

        // Assert
        assertTasks(0, 0, 2);
    }

    @Test
    void queueSynchronousTasksNoLimit() {
        // Arrange
        driver = new ConcurrentTaskDriver();

        // Act
        driver.queueSynchronous(Arrays.asList(() -> { }, () -> { }));

        // Assert
        assertTasks(0, 0, 2);
    }

    @Test
    void exceptionPassesThroughAsynchronousTaskAndTaskIsNotDone() {
        driver = new ConcurrentTaskDriver();

        Assertions.assertThrows(RuntimeException.class, () ->
                driver.queue(onDone -> {
                    throw new RuntimeException();
                })
        );
        assertTasks(0, 1, 0);
    }

    @Test
    void exceptionPassesThroughSynchronousTaskAndTaskIsDone() {
        driver = new ConcurrentTaskDriver();

        Assertions.assertThrows(RuntimeException.class, () ->
                driver.queue(() -> {
                    throw new RuntimeException();
                })
        );
        assertTasks(0, 0, 1);
    }

    @Test
    void finishingAsynchronousTaskMultipleTimesHasNoEffect() {
        // Arrange
        driver = new ConcurrentTaskDriver();
        AtomicReference<Runnable> finish = new AtomicReference<>();

        // Act
        driver.queue(finish::set);
        finish.get().run();
        finish.get().run();
        finish.get().run();

        // Assert
        assertTasks(0, 0, 1);
    }
}
