package com.github.trosenkrantz.sync.util.concurrency;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class ConcurrentTaskDriverLoadTest extends ConcurrentTaskDriverTest {

    public static final int MAX_RUNNING_TASKS = 4;

    @Test
    @Timeout(value = 8)
    void testLoad() {
        load(1000, 100000);
    }

    private void load(final int size, final int iterations) {
        driver = new ConcurrentTaskDriver(MAX_RUNNING_TASKS);
        long start = System.currentTimeMillis();

        for (int i = 0; i < size; i++) {
            driver.queue(asynchronousTask);
        }
        assertTasks(size - MAX_RUNNING_TASKS, MAX_RUNNING_TASKS, 0);

        for (int i = 0; i < iterations; i++) {
            driver.queue(asynchronousTask);
            finishTask();
        }
        assertTasks(size - MAX_RUNNING_TASKS, MAX_RUNNING_TASKS, iterations);

        for (int i = 0; i < size; i++) {
            finishTask();
        }
        assertTasks(0, 0, size + iterations);
    }
}
