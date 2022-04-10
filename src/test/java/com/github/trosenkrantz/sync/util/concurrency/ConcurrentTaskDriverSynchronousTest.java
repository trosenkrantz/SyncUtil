package com.github.trosenkrantz.sync.util.concurrency;

import org.junit.jupiter.api.Test;

class ConcurrentTaskDriverSynchronousTest extends ConcurrentTaskDriverTest {
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
        driver.queue(
                () -> assertTasks(1, 1, 0),
                () -> assertTasks(0, 1, 1)
        );

        // Assert
        assertTasks(0, 0, 2);
    }

    @Test
    void queueSynchronousTasksNoLimit() {
        // Arrange
        driver = new ConcurrentTaskDriver();

        // Act
        driver.queue(
                () -> {},
                () -> {}
        );

        // Assert
        assertTasks(0, 0, 2);
    }
}
