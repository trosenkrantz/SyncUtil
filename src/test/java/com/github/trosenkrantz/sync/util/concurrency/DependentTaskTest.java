package com.github.trosenkrantz.sync.util.concurrency;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

class DependentTaskTest extends ConcurrentTaskDriverTest {
    AtomicInteger notifiedCount;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        driver = new ConcurrentTaskDriver();
        notifiedCount = new AtomicInteger();
    }

    @Test
    void noDependencies() {
        DependentTask dependentTask = new DependentTask(asynchronousTask);
        dependentTask.schedule(driver);
        assertTasks(0, 1, 0); // Queued immediately

        finishTask();
        assertTasks(0, 0, 1);
    }

    @Test
    void dependentOnBothAsynchronousAndSynchronousTasks() {
        NotifyingTask task1 = new NotifyingTask(() -> {
            Assertions.assertEquals(0, notifiedCount.get()); // Not notified yet
        });
        driver.queue(task1);

        NotifyingTask task2 = new NotifyingTask(asynchronousTask);
        driver.queue(task2);

        NotifyingTask task3 = new NotifyingTask(() -> {
            Assertions.assertEquals(0, notifiedCount.get()); // Not notified yet
        });
        driver.queue(task3);

        DependentTask dependentTask = new DependentTask(notifiedCount::getAndIncrement);
        dependentTask.schedule(driver, task1, task2, task3);
        Assertions.assertEquals(0, notifiedCount.get()); // Not notified yet

        finishTask();
        Assertions.assertEquals(1, notifiedCount.get()); // Notified now
        assertTasks(0, 0, 4);
    }

    @Test
    void sequenceOfDependentTasks() {
        DependentTask task1 = new DependentTask(asynchronousTask);
        task1.schedule(driver);
        DependentTask task2 = new DependentTask(asynchronousTask);
        task2.schedule(driver, task1);
        DependentTask task3 = new DependentTask(asynchronousTask);
        task3.schedule(driver, task2);
        DependentTask task4 = new DependentTask(asynchronousTask);
        task4.schedule(driver, task3);

        assertTasks(0, 1, 0);

        finishTask();
        assertTasks(0, 1, 1);

        finishTask();
        assertTasks(0, 1, 2);

        finishTask();
        assertTasks(0, 1, 3);

        finishTask();
        assertTasks(0, 0, 4);
    }
}
