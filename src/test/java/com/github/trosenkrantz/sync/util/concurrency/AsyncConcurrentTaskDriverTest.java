package com.github.trosenkrantz.sync.util.concurrency;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AsyncConcurrentTaskDriverTest {
    private static void assertTasks(final AsyncConcurrentTaskDriver driver, final int expectedQueued, final int expectedRunning, final int expectedFinished) {
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
        AsyncConcurrentTaskDriver driver = new AsyncConcurrentTaskDriver(1);

        driver.addTask(() -> {});
        driver.addTask(() -> {});
        assertTasks(driver, 1, 1, 0);

        driver.onTaskDone();
        assertTasks(driver, 0, 1, 1);

        driver.onTaskDone();
        assertTasks(driver, 0, 0, 2);
    }

    @Test
    void max2RunningTasks() {
        AsyncConcurrentTaskDriver driver = new AsyncConcurrentTaskDriver(2);

        driver.addTask(() -> {});
        driver.addTask(() -> {});
        driver.addTask(() -> {});
        assertTasks(driver, 1, 2, 0);

        driver.onTaskDone();
        assertTasks(driver, 0, 2, 1);

        driver.onTaskDone();
        assertTasks(driver, 0, 1, 2);

        driver.onTaskDone();
        assertTasks(driver, 0, 0, 3);
    }

    @Test
    void noMaxRunningTasks() {
        AsyncConcurrentTaskDriver driver = new AsyncConcurrentTaskDriver();

        driver.addTask(() -> {});
        driver.addTask(() -> {});
        assertTasks(driver, 0, 2, 0);

        driver.onTaskDone();
        assertTasks(driver, 0, 1, 1);

        driver.onTaskDone();
        assertTasks(driver, 0, 0, 2);
    }

    @Test
    void increaseMaxRunningTasks() {
        AsyncConcurrentTaskDriver driver = new AsyncConcurrentTaskDriver(1);

        driver.addTask(() -> {});
        driver.addTask(() -> {});
        assertTasks(driver, 1, 1, 0);

        driver.setMaxRunningTasks(2);
        assertTasks(driver, 0, 2, 0);

        driver.onTaskDone();
        assertTasks(driver, 0, 1, 1);

        driver.onTaskDone();
        assertTasks(driver, 0, 0, 2);
    }

    @Test
    void reduceMaxRunningTasks() {
        AsyncConcurrentTaskDriver driver = new AsyncConcurrentTaskDriver(2);

        driver.addTask(() -> {});
        driver.addTask(() -> {});
        driver.addTask(() -> {});
        assertTasks(driver, 1, 2, 0);

        driver.setMaxRunningTasks(1);
        assertTasks(driver, 1, 2, 0);

        driver.onTaskDone();
        assertTasks(driver, 1, 1, 1);

        driver.onTaskDone();
        assertTasks(driver, 0, 1, 2);

        driver.onTaskDone();
        assertTasks(driver, 0, 0, 3);
    }

    @Test
    void listener() {
        AsyncConcurrentTaskDriver driver = new AsyncConcurrentTaskDriver(1);
        TestListener listener = new TestListener();
        driver.addListener(listener);

        driver.addTask(() -> {});
        driver.addTask(() -> {});
        assertTasks(listener, 1, 1, 0);

        driver.onTaskDone();
        assertTasks(listener, 0, 1, 1);

        driver.removeListener(listener);
        driver.onTaskDone();
        assertTasks(listener, 0, 1, 1);
    }

    @Test
    void cancelNewTasks() {
        AsyncConcurrentTaskDriver driver = new AsyncConcurrentTaskDriver(1);

        driver.addTask(() -> {});
        driver.addTask(() -> {});
        assertTasks(driver, 1, 1, 0);

        driver.cancelNewTasks();
        assertTasks(driver, 0, 1, 0);

        driver.onTaskDone();
        assertTasks(driver, 0, 0, 1);
    }
}