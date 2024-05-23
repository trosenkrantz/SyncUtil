package com.github.trosenkrantz.sync.util.concurrency;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
        driver = new ConcurrentTaskDriver();
        driver.setMaxRunningTasks(Limit.of(1));

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

    @Test
    void queueSynchronousTasksWithPriorityOrder() {
        // Arrange
        driver = new ConcurrentTaskDriver();
        driver.setMaxRunningTasks(Limit.of(1));
        driver.queue(asynchronousTask); // Dummy task to block driver from starting other tasks
        List<String> executionOrder = new ArrayList<>();

        // Act
        driver.queue(1, () -> executionOrder.add("Task 1"));
        driver.queue(2, () -> executionOrder.add("Task 2"));
        finishTask(); // Finish dummy task

        // Assert
        Assertions.assertEquals(2, executionOrder.size());
        Assertions.assertEquals("Task 1", executionOrder.get(0)); // Task 1 is executed first
        Assertions.assertEquals("Task 2", executionOrder.get(1));
        assertTasks(0, 0, 3);
    }

    @Test
    void queueSynchronousTasksAgainstPriorityOrder() {
        // Arrange
        driver = new ConcurrentTaskDriver();
        driver.setMaxRunningTasks(Limit.of(1));
        driver.queue(asynchronousTask); // Dummy task to block driver from starting other tasks
        List<String> executionOrder = new ArrayList<>();

        // Act
        driver.queue(2, () -> executionOrder.add("Task 1"));
        driver.queue(1, () -> executionOrder.add("Task 2"));
        finishTask(); // Finish dummy task

        // Assert
        Assertions.assertEquals(2, executionOrder.size());
        Assertions.assertEquals("Task 2", executionOrder.get(0));// Task 2 is executed first
        Assertions.assertEquals("Task 1", executionOrder.get(1));
        assertTasks(0, 0, 3);
    }
}
