package com.github.trosenkrantz.sync.util.concurrency;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

class ConcurrentTaskDriverThrottlingTest extends ConcurrentTaskDriverTest {
    @Test
    void max1RunningTask() {
        driver = new ConcurrentTaskDriver(1);

        driver.queueAsynchronous(Arrays.asList(asynchronousTask, asynchronousTask));
        assertTasks(1, 1, 0);

        finishTask();
        assertTasks(0, 1, 1);

        finishTask();
        assertTasks(0, 0, 2);
    }

    @Test
    void max2RunningTasks() {
        driver = new ConcurrentTaskDriver(2);

        driver.queueAsynchronous(Arrays.asList(asynchronousTask, asynchronousTask, asynchronousTask));
        assertTasks(1, 2, 0);

        finishTask();
        assertTasks(0, 2, 1);

        finishTask();
        assertTasks(0, 1, 2);

        finishTask();
        assertTasks(0, 0, 3);
    }

    @Test
    void noMaxRunningTasks() {
        driver = new ConcurrentTaskDriver();

        driver.queueAsynchronous(Arrays.asList(asynchronousTask, asynchronousTask));
        assertTasks(0, 2, 0);

        finishTask();
        assertTasks(0, 1, 1);

        finishTask();
        assertTasks(0, 0, 2);
    }

    @Test
    void increaseMaxRunningTasks() {
        driver = new ConcurrentTaskDriver(1);

        driver.queueAsynchronous(Arrays.asList(asynchronousTask, asynchronousTask));
        assertTasks(1, 1, 0);

        driver.setMaxRunningTasks(2);
        assertTasks(0, 2, 0);

        finishTask();
        assertTasks(0, 1, 1);

        finishTask();
        assertTasks(0, 0, 2);
    }

    @Test
    void reduceMaxRunningTasks() {
        driver = new ConcurrentTaskDriver(2);

        driver.queueAsynchronous(Arrays.asList(asynchronousTask, asynchronousTask, asynchronousTask));
        assertTasks(1, 2, 0);

        driver.setMaxRunningTasks(1);
        assertTasks(1, 2, 0);

        finishTask();
        assertTasks(1, 1, 1);

        finishTask();
        assertTasks(0, 1, 2);

        finishTask();
        assertTasks(0, 0, 3);
    }
}
