package com.github.trosenkrantz.sync.util.concurrency;

import org.junit.jupiter.api.Test;

class ConcurrentTaskDriverThrottlingTest extends ConcurrentTaskDriverTest {
    @Test
    void max1RunningTask() {
        driver = new ConcurrentTaskDriver();
        driver.setMaxRunningTasks(Limit.of(1));

        driver.queue(asynchronousTask, asynchronousTask);
        assertTasks(1, 1, 0);

        finishTask();
        assertTasks(0, 1, 1);

        finishTask();
        assertTasks(0, 0, 2);
    }

    @Test
    void max2RunningTasks() {
        driver = new ConcurrentTaskDriver();
        driver.setMaxRunningTasks(Limit.of(2));

        driver.queue(asynchronousTask, asynchronousTask, asynchronousTask);
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

        driver.queue(asynchronousTask, asynchronousTask);
        assertTasks(0, 2, 0);

        finishTask();
        assertTasks(0, 1, 1);

        finishTask();
        assertTasks(0, 0, 2);
    }

    @Test
    void increaseMaxRunningTasks() {
        driver = new ConcurrentTaskDriver();
        driver.setMaxRunningTasks(Limit.of(1));

        driver.queue(asynchronousTask, asynchronousTask);
        assertTasks(1, 1, 0);

        driver.setMaxRunningTasks(Limit.of(2));
        assertTasks(0, 2, 0);

        finishTask();
        assertTasks(0, 1, 1);

        finishTask();
        assertTasks(0, 0, 2);
    }

    @Test
    void reduceMaxRunningTasks() {
        driver = new ConcurrentTaskDriver();
        driver.setMaxRunningTasks(Limit.of(2));

        driver.queue(asynchronousTask, asynchronousTask, asynchronousTask);
        assertTasks(1, 2, 0);

        driver.setMaxRunningTasks(Limit.of(1));
        assertTasks(1, 2, 0);

        finishTask();
        assertTasks(1, 1, 1);

        finishTask();
        assertTasks(0, 1, 2);

        finishTask();
        assertTasks(0, 0, 3);
    }
}
