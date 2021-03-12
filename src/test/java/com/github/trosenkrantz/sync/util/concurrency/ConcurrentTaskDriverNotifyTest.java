package com.github.trosenkrantz.sync.util.concurrency;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

class ConcurrentTaskDriverNotifyTest extends ConcurrentTaskDriverTest {
    AtomicInteger doneCount;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        driver = new ConcurrentTaskDriver();
        doneCount = new AtomicInteger();
    }

    @Test
    void notifiedWhenAllTasksDone() {
        driver.queue(new NotifyingTaskList(doneCount::getAndIncrement, asynchronousTask, asynchronousTask, asynchronousTask));
        finishTask();
        finishTask();
        Assertions.assertEquals(0, doneCount.get()); // Not notified yet

        finishTask();
        Assertions.assertEquals(1, doneCount.get()); // Called now
        assertTasks(0, 0, 3);
    }

    @Test
    void notifiedIfCollectionIsMixedBetweenOtherTasks() {
        final Runnable[] collectionTasksOnDone = new Runnable[1];

        driver.queue(asynchronousTask);
        driver.queue(new NotifyingTaskList(doneCount::getAndIncrement, onDone -> collectionTasksOnDone[0] = onDone));
        driver.queue(asynchronousTask);
        finishTask();
        finishTask();
        Assertions.assertEquals(0, doneCount.get()); // Not notified yet

        collectionTasksOnDone[0].run(); // Now finish collection task
        Assertions.assertEquals(1, doneCount.get()); // Called now
        assertTasks(0, 0, 3);
    }

    @Test
    void synchronousTask() {
        driver.queue(new NotifyingTaskList(doneCount::getAndIncrement, () -> {
            Assertions.assertEquals(0, doneCount.get()); // Not notified yet
            assertTasks(0, 1, 0);
        }));
        Assertions.assertEquals(1, doneCount.get()); // Called now
        assertTasks(0, 0, 1);
    }

    @Test
    void mixedAsynchronousAndSynchronousTasks() {
        NotifyingTaskList taskListUnderTest = new NotifyingTaskList(doneCount::getAndIncrement);
        taskListUnderTest.add(() -> {
            Assertions.assertEquals(0, doneCount.get()); // Not notified yet
            assertTasks(2, 1, 0);
        });
        taskListUnderTest.add(asynchronousTask);
        taskListUnderTest.add(() -> {
            Assertions.assertEquals(0, doneCount.get()); // Not notified yet
            assertTasks(0, 2, 1);
        });

        driver.queue(taskListUnderTest);
        Assertions.assertEquals(0, doneCount.get()); // Not notified yet

        finishTask();
        Assertions.assertEquals(1, doneCount.get()); // Called now
        assertTasks(0, 0, 3);
    }

    @Test
    void exceptionPassesThroughSynchronousTaskAndTaskIsDoneAndWeAreNotified() {
        NotifyingTaskList tasks = new NotifyingTaskList(doneCount::getAndIncrement, () -> {
            throw new RuntimeException();
        });
        Assertions.assertEquals(0, doneCount.get()); // Not notified yet

        Assertions.assertThrows(RuntimeException.class, () -> driver.queue(tasks));

        Assertions.assertEquals(1, doneCount.get()); // Called now
        assertTasks(0, 0, 1);
    }

    @Test
    void reuseNotifyingTasks() {
        NotifyingTaskList tasks = new NotifyingTaskList(doneCount::getAndIncrement, asynchronousTask);
        driver.queue(tasks);
        finishTask();
        Assertions.assertEquals(1, doneCount.get()); // Notified once
        assertTasks(0, 0, 1);

        driver.queue(tasks);
        assertTasks(0, 1, 1);
        Assertions.assertEquals(1, doneCount.get()); // Not notified a second time yet

        finishTask();
        Assertions.assertEquals(2, doneCount.get()); // Notified twice
        assertTasks(0, 0, 2);
    }

    @Test
    void reuseNotifyingTasksButAddingMore() {
        NotifyingTaskList tasks = new NotifyingTaskList(doneCount::getAndIncrement, asynchronousTask);
        driver.queue(tasks);
        finishTask();
        Assertions.assertEquals(1, doneCount.get()); // Notified once
        assertTasks(0, 0, 1);

        // Add task to notifying list
        tasks.add(asynchronousTask);

        driver.queue(tasks);
        assertTasks(0, 2, 1);

        finishTask();
        assertTasks(0, 1, 2);
        Assertions.assertEquals(1, doneCount.get()); // Not notified a second time yet

        finishTask();
        Assertions.assertEquals(2, doneCount.get()); // Notified twice
        assertTasks(0, 0, 3);
    }
}
