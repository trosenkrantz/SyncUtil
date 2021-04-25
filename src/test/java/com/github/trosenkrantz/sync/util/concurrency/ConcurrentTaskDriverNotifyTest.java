package com.github.trosenkrantz.sync.util.concurrency;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

class ConcurrentTaskDriverNotifyTest extends ConcurrentTaskDriverTest {
    AtomicInteger notifiedCount;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        driver = new ConcurrentTaskDriver();
        notifiedCount = new AtomicInteger();
    }

    @Test
    void notifiedWhenAllTasksDone() {
        driver.queue(new NotifyingTaskList(notifiedCount::getAndIncrement, asynchronousTask, asynchronousTask, asynchronousTask));
        finishTask();
        finishTask();
        Assertions.assertEquals(0, notifiedCount.get()); // Not notified yet

        finishTask();
        Assertions.assertEquals(1, notifiedCount.get()); // Notified now
        assertTasks(0, 0, 3);
    }

    @Test
    void notifiedIfCollectionIsMixedBetweenOtherTasks() {
        final Runnable[] collectionTasksOnDone = new Runnable[1];

        driver.queue(asynchronousTask);
        driver.queue(new NotifyingTaskList(notifiedCount::getAndIncrement, onDone -> collectionTasksOnDone[0] = onDone));
        driver.queue(asynchronousTask);
        finishTask(); // Finish task that were queued before notifying task
        finishTask(); // Finish task that were queued after notifying task
        Assertions.assertEquals(0, notifiedCount.get()); // Not notified yet

        collectionTasksOnDone[0].run(); // Now finish notifying task
        Assertions.assertEquals(1, notifiedCount.get()); // Notified now
        assertTasks(0, 0, 3);
    }

    @Test
    void synchronousTask() {
        driver.queue(new NotifyingTaskList(notifiedCount::getAndIncrement, () -> {
            Assertions.assertEquals(0, notifiedCount.get()); // Not notified yet
            assertTasks(0, 1, 0);
        }));
        Assertions.assertEquals(1, notifiedCount.get()); // Notified now
        assertTasks(0, 0, 1);
    }

    @Test
    void mixedAsynchronousAndSynchronousTasks() {
        NotifyingTaskList taskListUnderTest = new NotifyingTaskList(notifiedCount::getAndIncrement);
        taskListUnderTest.add(() -> {
            Assertions.assertEquals(0, notifiedCount.get()); // Not notified yet
            assertTasks(2, 1, 0);
        });
        taskListUnderTest.add(asynchronousTask);
        taskListUnderTest.add(() -> {
            Assertions.assertEquals(0, notifiedCount.get()); // Not notified yet
            assertTasks(0, 2, 1);
        });

        driver.queue(taskListUnderTest);
        Assertions.assertEquals(0, notifiedCount.get()); // Not notified yet

        finishTask();
        Assertions.assertEquals(1, notifiedCount.get()); // Notified now
        assertTasks(0, 0, 3);
    }

    @Test
    void exceptionPassesThroughSynchronousTaskAndTaskIsDoneAndWeAreNotified() {
        NotifyingTaskList tasks = new NotifyingTaskList(notifiedCount::getAndIncrement, () -> {
            throw new RuntimeException();
        });
        Assertions.assertEquals(0, notifiedCount.get()); // Not notified yet

        Assertions.assertThrows(RuntimeException.class, () -> driver.queue(tasks));

        Assertions.assertEquals(1, notifiedCount.get()); // Notified now
        assertTasks(0, 0, 1);
    }

    @Test
    void reuseNotifyingTasks() {
        NotifyingTaskList tasks = new NotifyingTaskList(notifiedCount::getAndIncrement, asynchronousTask);
        driver.queue(tasks);
        finishTask();
        Assertions.assertEquals(1, notifiedCount.get()); // Notified once
        assertTasks(0, 0, 1);

        driver.queue(tasks);
        assertTasks(0, 1, 1);
        Assertions.assertEquals(1, notifiedCount.get()); // Not notified a second time yet

        finishTask();
        Assertions.assertEquals(2, notifiedCount.get()); // Notified twice
        assertTasks(0, 0, 2);
    }

    @Test
    void reuseNotifyingTasksButAddMore() {
        NotifyingTaskList tasks = new NotifyingTaskList(notifiedCount::getAndIncrement, asynchronousTask);
        driver.queue(tasks);
        finishTask();
        Assertions.assertEquals(1, notifiedCount.get()); // Notified once
        assertTasks(0, 0, 1);

        // Add task to notifying list
        tasks.add(asynchronousTask);

        driver.queue(tasks);
        assertTasks(0, 2, 1);

        finishTask();
        assertTasks(0, 1, 2);
        Assertions.assertEquals(1, notifiedCount.get()); // Not notified a second time yet

        finishTask();
        Assertions.assertEquals(2, notifiedCount.get()); // Notified twice
        assertTasks(0, 0, 3);
    }
}
