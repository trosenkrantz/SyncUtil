package com.github.trosenkrantz.sync.util.concurrency;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class ConcurrentTaskDriverSuspendTest extends ConcurrentTaskDriverTest {

    @Test
    void suspend() {
        driver = new ConcurrentTaskDriver(2);
        driver.queueAsynchronous(Arrays.asList(asynchronousTask, asynchronousTask, asynchronousTask));

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
        driver = new ConcurrentTaskDriver(2);
        driver.queueAsynchronous(Arrays.asList(asynchronousTask, asynchronousTask, asynchronousTask));
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
        driver = new ConcurrentTaskDriver(1);

        driver.suspend();
        driver.queueAsynchronous(Arrays.asList(asynchronousTask, asynchronousTask));
        assertTasks(2, 0, 0);

        driver.resume();
        assertTasks(1, 1, 0);
    }

    @Test
    void suspendMultipleTimes() {
        driver = new ConcurrentTaskDriver(1);
        driver.queueAsynchronous(Arrays.asList(asynchronousTask, asynchronousTask, asynchronousTask));

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
}
