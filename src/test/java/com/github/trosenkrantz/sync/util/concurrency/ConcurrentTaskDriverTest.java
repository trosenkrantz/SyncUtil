package com.github.trosenkrantz.sync.util.concurrency;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class ConcurrentTaskDriverTest {
    private static void assertTasks(final ConcurrentTaskDriver driver, final int expectedQueued, final int expectedRunning, final int expectedFinished) {
        Assertions.assertEquals(expectedQueued, driver.getNumberOfQueuedTasks());
        Assertions.assertEquals(expectedRunning, driver.getNumberOfRunningTasks());
        Assertions.assertEquals(expectedFinished, driver.getNumberOfFinishedTasks());
    }

    private static void assertTasks(final TestListener listener, final int expectedQueued, final int expectedRunning, final int expectedFinished) {
        Assertions.assertEquals(expectedQueued, listener.getQueued());
        Assertions.assertEquals(expectedRunning, listener.getRunning());
        Assertions.assertEquals(expectedFinished, listener.getFinished());
    }

    @Test
    void max1RunningTask() {
        ConcurrentTaskDriver driver = new ConcurrentTaskDriver(1);

        driver.queueAsynchronousTask(() -> { });
        driver.queueAsynchronousTask(() -> { });
        assertTasks(driver, 1, 1, 0);

        driver.onAsynchronousTaskDone();
        assertTasks(driver, 0, 1, 1);

        driver.onAsynchronousTaskDone();
        assertTasks(driver, 0, 0, 2);
    }

    @Test
    void max2RunningTasks() {
        ConcurrentTaskDriver driver = new ConcurrentTaskDriver(2);

        driver.queueAsynchronousTask(() -> { });
        driver.queueAsynchronousTask(() -> { });
        driver.queueAsynchronousTask(() -> { });
        assertTasks(driver, 1, 2, 0);

        driver.onAsynchronousTaskDone();
        assertTasks(driver, 0, 2, 1);

        driver.onAsynchronousTaskDone();
        assertTasks(driver, 0, 1, 2);

        driver.onAsynchronousTaskDone();
        assertTasks(driver, 0, 0, 3);
    }

    @Test
    void noMaxRunningTasks() {
        ConcurrentTaskDriver driver = new ConcurrentTaskDriver();

        driver.queueAsynchronousTask(() -> { });
        driver.queueAsynchronousTask(() -> { });
        assertTasks(driver, 0, 2, 0);

        driver.onAsynchronousTaskDone();
        assertTasks(driver, 0, 1, 1);

        driver.onAsynchronousTaskDone();
        assertTasks(driver, 0, 0, 2);
    }

    @Test
    void increaseMaxRunningTasks() {
        ConcurrentTaskDriver driver = new ConcurrentTaskDriver(1);

        driver.queueAsynchronousTask(() -> { });
        driver.queueAsynchronousTask(() -> { });
        assertTasks(driver, 1, 1, 0);

        driver.setMaxRunningTasks(2);
        assertTasks(driver, 0, 2, 0);

        driver.onAsynchronousTaskDone();
        assertTasks(driver, 0, 1, 1);

        driver.onAsynchronousTaskDone();
        assertTasks(driver, 0, 0, 2);
    }

    @Test
    void reduceMaxRunningTasks() {
        ConcurrentTaskDriver driver = new ConcurrentTaskDriver(2);

        driver.queueAsynchronousTask(() -> { });
        driver.queueAsynchronousTask(() -> { });
        driver.queueAsynchronousTask(() -> { });
        assertTasks(driver, 1, 2, 0);

        driver.setMaxRunningTasks(1);
        assertTasks(driver, 1, 2, 0);

        driver.onAsynchronousTaskDone();
        assertTasks(driver, 1, 1, 1);

        driver.onAsynchronousTaskDone();
        assertTasks(driver, 0, 1, 2);

        driver.onAsynchronousTaskDone();
        assertTasks(driver, 0, 0, 3);
    }

    @Test
    void listener() {
        ConcurrentTaskDriver driver = new ConcurrentTaskDriver(1);
        TestListener listener = new TestListener();
        driver.addListener(listener);

        driver.queueAsynchronousTask(() -> { });
        driver.queueAsynchronousTask(() -> { });
        assertTasks(listener, 1, 1, 0);

        driver.onAsynchronousTaskDone();
        assertTasks(listener, 0, 1, 1);

        driver.removeListener(listener);
        driver.onAsynchronousTaskDone();
        assertTasks(listener, 0, 1, 1);
    }

    @Test
    void clearQueue() {
        ConcurrentTaskDriver driver = new ConcurrentTaskDriver(1);

        driver.queueAsynchronousTasks(Arrays.asList(() -> { }, () -> { }));
        assertTasks(driver, 1, 1, 0);

        driver.clearQueue();
        assertTasks(driver, 0, 1, 0);

        driver.onAsynchronousTaskDone();
        assertTasks(driver, 0, 0, 1);
    }

    @Test
    void queueSynchronousTask() {
        ConcurrentTaskDriver driver = new ConcurrentTaskDriver();

        driver.queueSynchronousTask(() -> { });
        assertTasks(driver, 0, 0, 1);
    }

    @Test
    void queueSynchronousTasksLimit1() {
        ConcurrentTaskDriver driver = new ConcurrentTaskDriver(1);
        // Arrange

        // Act
        driver.queueSynchronousTasks(Arrays.asList(() -> { }, () -> { }));

        // Assert
        assertTasks(driver, 0, 0, 2);
    }

    @Test
    void queueSynchronousTasksNoLimit() {
        // Arrange
        ConcurrentTaskDriver driver = new ConcurrentTaskDriver();

        // Act
        driver.queueSynchronousTasks(Arrays.asList(() -> { }, () -> { }));

        // Assert
        assertTasks(driver, 0, 0, 2);
    }

    @Test
    void suspend() {
        ConcurrentTaskDriver driver = new ConcurrentTaskDriver(2);
        driver.queueAsynchronousTasks(Arrays.asList(() -> { }, () -> { }, () -> { }));

        driver.suspend();
        assertTasks(driver, 1, 2, 0);

        driver.onAsynchronousTaskDone();
        assertTasks(driver, 1, 1, 1);

        driver.onAsynchronousTaskDone();
        assertTasks(driver, 1, 0, 2);

        driver.resume();
        assertTasks(driver, 0, 1, 2);

        driver.onAsynchronousTaskDone();
        assertTasks(driver, 0, 0, 3);
    }

    @Test
    void suspendWithCallback() {
        ConcurrentTaskDriver driver = new ConcurrentTaskDriver(2);
        driver.queueAsynchronousTasks(Arrays.asList(() -> { }, () -> { }, () -> { }));
        final int[] whenIdleCalledCount = {0};
        Runnable whenIdle = () -> whenIdleCalledCount[0]++;

        driver.suspend(whenIdle);
        assertTasks(driver, 1, 2, 0);

        driver.onAsynchronousTaskDone();
        assertTasks(driver, 1, 1, 1);
        Assertions.assertEquals(0, whenIdleCalledCount[0]);

        driver.onAsynchronousTaskDone();
        assertTasks(driver, 1, 0, 2);

        driver.resume();
        assertTasks(driver, 0, 1, 2);

        driver.onAsynchronousTaskDone();
        assertTasks(driver, 0, 0, 3);
        Assertions.assertEquals(1, whenIdleCalledCount[0]);
    }

    @Test
    void suspendFromStart() {
        ConcurrentTaskDriver driver = new ConcurrentTaskDriver(1);

        driver.suspend();
        driver.queueAsynchronousTasks(Arrays.asList(() -> { }, () -> { }));
        assertTasks(driver, 2, 0, 0);

        driver.resume();
        assertTasks(driver, 1, 1, 0);
    }

    @Test
    void suspendMultipleTimes() {
        ConcurrentTaskDriver driver = new ConcurrentTaskDriver(1);
        driver.queueAsynchronousTasks(Arrays.asList(() -> { }, () -> { }, () -> { }));

        driver.suspend();
        driver.onAsynchronousTaskDone();
        assertTasks(driver, 2, 0, 1);

        driver.resume();
        assertTasks(driver, 1, 1, 1);

        driver.suspend();
        assertTasks(driver, 1, 1, 1);

        driver.onAsynchronousTaskDone();
        assertTasks(driver, 1, 0, 2);
    }
}