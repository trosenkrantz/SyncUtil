package com.github.trosenkrantz.sync.util.concurrency;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class ConcurrentTaskDriverLoadTest extends ConcurrentTaskDriverTest {
    public static final int MAX_RUNNING_TASKS = 4;
    public static final int QUEUE_SIZE = 1000;
    public static final int TASK_COUNT = 1000000;

    @Test
    @Timeout(value = 8)
    void loadWithoutPriorityTasks() {
        driver = new ConcurrentTaskDriver();
        driver.setMaxRunningTasks(Limit.of(MAX_RUNNING_TASKS));

        for (int i = 0; i < QUEUE_SIZE; i++) {
            driver.queue(asynchronousTask);
        }
        assertTasks(QUEUE_SIZE - MAX_RUNNING_TASKS, MAX_RUNNING_TASKS, 0);

        for (int i = 0; i < TASK_COUNT - QUEUE_SIZE; i++) {
            driver.queue(asynchronousTask);
            finishTask();
        }
        assertTasks(QUEUE_SIZE - MAX_RUNNING_TASKS, MAX_RUNNING_TASKS, TASK_COUNT - QUEUE_SIZE);

        for (int i = 0; i < QUEUE_SIZE; i++) {
            finishTask();
        }
        assertTasks(0, 0, TASK_COUNT);
    }

    @Test
    @Timeout(value = 8)
    void loadWithPriorityTasks() {
        driver = new ConcurrentTaskDriver();
        driver.setMaxRunningTasks(Limit.of(MAX_RUNNING_TASKS));

        for (int i = 0; i < QUEUE_SIZE; i++) {
            driver.queue(i % 10, asynchronousTask);
        }
        assertTasks(QUEUE_SIZE - MAX_RUNNING_TASKS, MAX_RUNNING_TASKS, 0);

        for (int i = 0; i < TASK_COUNT - QUEUE_SIZE; i++) {
            driver.queue(i % 10, asynchronousTask);
            finishTask();
        }
        assertTasks(QUEUE_SIZE - MAX_RUNNING_TASKS, MAX_RUNNING_TASKS, TASK_COUNT - QUEUE_SIZE);

        for (int i = 0; i < QUEUE_SIZE; i++) {
            finishTask();
        }
        assertTasks(0, 0, TASK_COUNT);
    }
}
