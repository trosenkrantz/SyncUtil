package com.github.trosenkrantz.sync.util.concurrency;

import com.sun.tools.javac.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConcurrentTaskDriverTest {
    private static void assertTasks(final ConcurrentTaskDriver driver, final int expectedQueued, final int expectedRunning, final int expectedFinished) {
        assertEquals(expectedQueued, driver.getNumberOfQueuedTasks());
        assertEquals(expectedRunning, driver.getNumberOfRunningTasks());
        assertEquals(expectedFinished, driver.getNumberOfFinishedTasks());
    }

    private static void assertTasks(final TestListener listener, final int expectedQueued, final int expectedRunning, final int expectedFinished) {
        assertEquals(expectedQueued, listener.getQueued());
        assertEquals(expectedRunning, listener.getRunning());
        assertEquals(expectedFinished, listener.getFinished());
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
    void cancelQueue() {
        ConcurrentTaskDriver driver = new ConcurrentTaskDriver(1);

        driver.queueAsynchronousTask(() -> { });
        driver.queueAsynchronousTask(() -> { });
        assertTasks(driver, 1, 1, 0);

        driver.cancelQueuedTasks();
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

        driver.queueSynchronousTasks(List.of(() -> { }, () -> {}));
        assertTasks(driver, 0, 0, 2);
    }

    @Test
    void queueSynchronousTasksNoLimit() {
        ConcurrentTaskDriver driver = new ConcurrentTaskDriver();

        driver.queueSynchronousTasks(List.of(() -> { }, () -> {}));
        assertTasks(driver, 0, 0, 2);
    }
}