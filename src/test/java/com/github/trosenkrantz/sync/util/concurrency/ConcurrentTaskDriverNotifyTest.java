package com.github.trosenkrantz.sync.util.concurrency;

import com.github.trosenkrantz.sync.util.runnable.SingleRunnable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

class ConcurrentTaskDriverNotifyTest extends ConcurrentTaskDriverTest {
    @Test
    void notifiedIfCollectionIsTheOnlyTasks() {
        driver = new ConcurrentTaskDriver();
        AtomicInteger doneCount = new AtomicInteger();

        driver.queueAsynchronous(Arrays.asList(asynchronousTask, asynchronousTask, asynchronousTask), doneCount::getAndIncrement);
        finishTask();
        finishTask();
        Assertions.assertEquals(0, doneCount.get()); // Not called yet

        finishTask();
        Assertions.assertEquals(1, doneCount.get()); // Called now
        assertTasks(0, 0, 3);
    }

    @Test
    void notifiedIfCollectionIsMixedBetweenOtherTasks() {
        driver = new ConcurrentTaskDriver();
        AtomicInteger doneCount = new AtomicInteger();
        final Runnable[] collectionTasksOnDone = new Runnable[1];

        driver.queue(asynchronousTask);
        driver.queueAsynchronous(Collections.singletonList(onDone -> collectionTasksOnDone[0] = onDone), doneCount::getAndIncrement);
        driver.queue(asynchronousTask);
        finishTask();
        finishTask();
        Assertions.assertEquals(0, doneCount.get()); // Not called yet

        collectionTasksOnDone[0].run(); // Now finish collection task
        Assertions.assertEquals(1, doneCount.get()); // Called now
        assertTasks(0, 0, 3);
    }
}
