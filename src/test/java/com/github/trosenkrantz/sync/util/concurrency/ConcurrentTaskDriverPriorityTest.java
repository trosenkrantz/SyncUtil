package com.github.trosenkrantz.sync.util.concurrency;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

class ConcurrentTaskDriverPriorityTest extends ConcurrentTaskDriverTest {
    @Test
    void keepsOrderIfQueuedInPriorityOrder() {
        driver = new ConcurrentTaskDriver();
        driver.setMaxRunningTasks(Limit.of(1));
        driver.queue(asynchronousTask); // Dummy task to block driver from starting other tasks

        AtomicReference<Runnable> task1OnDone = new AtomicReference<>();
        AtomicReference<Runnable> task2OnDone = new AtomicReference<>();

        driver.queue(1, task1OnDone::set);
        driver.queue(2, task2OnDone::set);
        Assertions.assertNull(task1OnDone.get());
        Assertions.assertNull(task2OnDone.get());
        assertTasks(2, 1, 0);

        finishTask(); // Finish dummy task
        Assertions.assertNotNull(task1OnDone.get()); // Task 1 has started
        Assertions.assertNull(task2OnDone.get());
        assertTasks(1, 1, 1);

        task1OnDone.get().run();
        Assertions.assertNotNull(task2OnDone.get()); // Task 2 has started
        assertTasks(0, 1, 2);

        task2OnDone.get().run();
        assertTasks(0, 0, 3);
    }

    @Test
    void changesOrderIfQueuedAgainstPriorityOrder() {
        driver = new ConcurrentTaskDriver();
        driver.setMaxRunningTasks(Limit.of(1));
        driver.queue(asynchronousTask); // Dummy task to block driver from starting other tasks

        AtomicReference<Runnable> task1OnDone = new AtomicReference<>();
        AtomicReference<Runnable> task2OnDone = new AtomicReference<>();

        driver.queue(2, task1OnDone::set);
        driver.queue(1, task2OnDone::set);
        Assertions.assertNull(task1OnDone.get());
        Assertions.assertNull(task2OnDone.get());
        assertTasks(2, 1, 0);

        finishTask(); // Finish dummy task
        Assertions.assertNull(task1OnDone.get());
        Assertions.assertNotNull(task2OnDone.get()); // Task 2 has started
        assertTasks(1, 1, 1);

        task2OnDone.get().run();
        Assertions.assertNotNull(task1OnDone.get()); // Task 1 has started
        assertTasks(0, 1, 2);

        task1OnDone.get().run();
        assertTasks(0, 0, 3);
    }

    @Test
    void keepsOrderIfPrioritisedTaskIsQueuedFirst() {
        driver = new ConcurrentTaskDriver();
        driver.setMaxRunningTasks(Limit.of(1));
        driver.queue(asynchronousTask); // Dummy task to block driver from starting other tasks

        AtomicReference<Runnable> taskWithPriorityOnDone = new AtomicReference<>();
        AtomicReference<Runnable> taskWithoutPriorityOnDone = new AtomicReference<>();

        driver.queue(1, taskWithPriorityOnDone::set);
        driver.queue(taskWithoutPriorityOnDone::set);
        Assertions.assertNull(taskWithPriorityOnDone.get());
        Assertions.assertNull(taskWithoutPriorityOnDone.get());
        assertTasks(2, 1, 0);

        finishTask(); // Finish dummy task
        Assertions.assertNotNull(taskWithPriorityOnDone.get()); // Task with priority has started
        Assertions.assertNull(taskWithoutPriorityOnDone.get());
        assertTasks(1, 1, 1);

        taskWithPriorityOnDone.get().run();
        Assertions.assertNotNull(taskWithoutPriorityOnDone.get()); // Task without priority has started
        assertTasks(0, 1, 2);

        taskWithoutPriorityOnDone.get().run();
        assertTasks(0, 0, 3);
    }

    @Test
    void changesOrderIfPrioritisedTaskIsQueuedLast() {
        driver = new ConcurrentTaskDriver();
        driver.setMaxRunningTasks(Limit.of(1));
        driver.queue(asynchronousTask); // Dummy task to block driver from starting other tasks

        AtomicReference<Runnable> taskWithPriorityOnDone = new AtomicReference<>();
        AtomicReference<Runnable> taskWithoutPriorityOnDone = new AtomicReference<>();

        driver.queue(taskWithoutPriorityOnDone::set);
        driver.queue(1, taskWithPriorityOnDone::set);
        Assertions.assertNull(taskWithPriorityOnDone.get());
        Assertions.assertNull(taskWithoutPriorityOnDone.get());
        assertTasks(2, 1, 0);

        finishTask(); // Finish dummy task
        Assertions.assertNotNull(taskWithPriorityOnDone.get()); // Task with priority has started
        Assertions.assertNull(taskWithoutPriorityOnDone.get());
        assertTasks(1, 1, 1);

        taskWithPriorityOnDone.get().run();
        Assertions.assertNotNull(taskWithoutPriorityOnDone.get()); // Task without priority has started
        assertTasks(0, 1, 2);

        taskWithoutPriorityOnDone.get().run();
        assertTasks(0, 0, 3);
    }
}
