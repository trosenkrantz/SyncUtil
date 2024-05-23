package com.github.trosenkrantz.sync.util.concurrency;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConcurrentTaskDriverSuspendTest extends ConcurrentTaskDriverTest {
    @Test
    void suspend() {
        driver = new ConcurrentTaskDriver();
        driver.setMaxRunningTasks(Limit.of(2));
        driver.queue(asynchronousTask, asynchronousTask, asynchronousTask);

        driver.suspend();
        assertTasks(1, 2, 0);

        finishTask();
        assertTasks(1, 1, 1);

        finishTask();
        assertTasks(1, 0, 2);

        driver.resume();
        assertTasks(0, 1, 2);

        finishTask();
        assertTasks(0, 0, 3);
    }

    @Test
    void suspendWithCallback() {
        driver = new ConcurrentTaskDriver();
        driver.setMaxRunningTasks(Limit.of(2));
        driver.queue(asynchronousTask, asynchronousTask, asynchronousTask);
        final int[] whenIdleCalledCount = {0};
        Runnable whenIdle = () -> whenIdleCalledCount[0]++;

        driver.suspend(whenIdle);
        assertTasks(1, 2, 0);

        finishTask();
        assertTasks(1, 1, 1);
        Assertions.assertEquals(0, whenIdleCalledCount[0]);

        finishTask();
        assertTasks(1, 0, 2);

        driver.resume();
        assertTasks(0, 1, 2);

        finishTask();
        assertTasks(0, 0, 3);
        Assertions.assertEquals(1, whenIdleCalledCount[0]);
    }

    @Test
    void suspendFromStart() {
        driver = new ConcurrentTaskDriver();
        driver.setMaxRunningTasks(Limit.of(1));

        driver.suspend();
        driver.queue(asynchronousTask, asynchronousTask);
        assertTasks(2, 0, 0);

        driver.resume();
        assertTasks(1, 1, 0);
    }

    @Test
    void suspendMultipleTimes() {
        driver = new ConcurrentTaskDriver();
        driver.setMaxRunningTasks(Limit.of(1));
        driver.queue(asynchronousTask, asynchronousTask, asynchronousTask);

        driver.suspend();
        finishTask();
        assertTasks(2, 0, 1);

        driver.resume();
        assertTasks(1, 1, 1);

        driver.suspend();
        assertTasks(1, 1, 1);

        finishTask();
        assertTasks(1, 0, 2);
    }

    @Test
    void suspendWhileIdleAndGetCallbackImmediately() {
        // Arrange
        driver = new ConcurrentTaskDriver();
        final int[] whenIdleCalledCount = {0};
        Runnable whenIdle = () -> whenIdleCalledCount[0]++;

        // Act
        driver.suspend(whenIdle);

        // Assert
        Assertions.assertEquals(1, whenIdleCalledCount[0]);
    }
}
