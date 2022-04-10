package com.github.trosenkrantz.sync.util.concurrency;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

class NotifyingTaskTest extends ConcurrentTaskDriverTest {
    AtomicInteger notifiedCount;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        driver = new ConcurrentTaskDriver();
        notifiedCount = new AtomicInteger();
    }

    @Test
    void withoutSubscribers() {
        driver.queue(new NotifyingTask(asynchronousTask));

        finishTask();
        assertTasks(0, 0, 1);
    }

    @Test
    void subscribeWithConstructor() {
        driver.queue(new NotifyingTask(asynchronousTask, notifiedCount::getAndIncrement));
        Assertions.assertEquals(0, notifiedCount.get()); // Not notified yet

        finishTask();
        Assertions.assertEquals(1, notifiedCount.get()); // Notified now
        assertTasks(0, 0, 1);
    }

    @Test
    void subscribeWithMethod() {
        NotifyingTask task = new NotifyingTask(asynchronousTask);
        driver.queue(task);
        task.subscribe(notifiedCount::getAndIncrement);
        Assertions.assertEquals(0, notifiedCount.get()); // Not notified yet

        finishTask();
        Assertions.assertEquals(1, notifiedCount.get()); // Notified now
        assertTasks(0, 0, 1);
    }

    @Test
    void subscribeWithConstructorAndMethod() {
        AtomicInteger notifiedCount1 = new AtomicInteger();
        AtomicInteger notifiedCount2 = new AtomicInteger();
        NotifyingTask task = new NotifyingTask(asynchronousTask, notifiedCount1::getAndIncrement);
        driver.queue(task);
        task.subscribe(notifiedCount2::getAndIncrement);
        Assertions.assertEquals(0, notifiedCount1.get()); // Not notified yet
        Assertions.assertEquals(0, notifiedCount2.get()); // Not notified yet

        finishTask();
        Assertions.assertEquals(1, notifiedCount1.get()); // Notified now
        Assertions.assertEquals(1, notifiedCount2.get()); // Notified now
        assertTasks(0, 0, 1);
    }

    @Test
    void notifiedWhenMultipleTasksDone() {
        driver.queue(new NotifyingTask(asynchronousTask, notifiedCount::getAndIncrement));
        driver.queue(new NotifyingTask(asynchronousTask, notifiedCount::getAndIncrement));
        driver.queue(new NotifyingTask(asynchronousTask, notifiedCount::getAndIncrement));

        finishTask();
        finishTask();
        Assertions.assertEquals(2, notifiedCount.get());

        finishTask();
        Assertions.assertEquals(3, notifiedCount.get());
        assertTasks(0, 0, 3);
    }

    @Test
    void notifiedIfCollectionIsMixedBetweenOtherTasks() {
        final AtomicReference<Runnable> notifyingTaskOneDone = new AtomicReference<>();
        NotifyingTask notifyingTask = new NotifyingTask(notifyingTaskOneDone::set, notifiedCount::getAndIncrement);

        driver.queue(asynchronousTask);
        driver.queue(notifyingTask);
        driver.queue(asynchronousTask);
        finishTask(); // Finish task that were queued before notifying task
        finishTask(); // Finish task that were queued after notifying task
        Assertions.assertEquals(0, notifiedCount.get()); // Not notified yet

        notifyingTaskOneDone.get().run(); // Now finish notifying task
        Assertions.assertEquals(1, notifiedCount.get()); // Notified now
        assertTasks(0, 0, 3);
    }

    @Test
    void synchronousTask() {
        driver.queue(new NotifyingTask(
                () -> {
                    Assertions.assertEquals(0, notifiedCount.get()); // Not notified yet
                    assertTasks(0, 1, 0);
                },
                notifiedCount::getAndIncrement
        ));
        Assertions.assertEquals(1, notifiedCount.get()); // Notified now
        assertTasks(0, 0, 1);
    }

    @Test
    void exceptionPassesThroughSynchronousTaskAndTaskIsDoneAndWeAreNotified() {
        NotifyingTask tasks = new NotifyingTask(() -> {
            throw new RuntimeException();
        }, notifiedCount::getAndIncrement);
        Assertions.assertEquals(0, notifiedCount.get()); // Not notified yet

        Assertions.assertThrows(RuntimeException.class, () -> driver.queue(tasks));

        Assertions.assertEquals(1, notifiedCount.get()); // Notified now
        assertTasks(0, 0, 1);
    }

    @Test
    void reuseNotifyingTasks() {
        NotifyingTask task = new NotifyingTask(asynchronousTask, notifiedCount::getAndIncrement);
        driver.queue(task);
        finishTask();
        Assertions.assertEquals(1, notifiedCount.get()); // Notified once
        assertTasks(0, 0, 1);

        driver.queue(task);
        assertTasks(0, 1, 1);
        Assertions.assertEquals(1, notifiedCount.get()); // Not notified a second time yet

        finishTask();
        Assertions.assertEquals(2, notifiedCount.get()); // Notified twice
        assertTasks(0, 0, 2);
    }
}
