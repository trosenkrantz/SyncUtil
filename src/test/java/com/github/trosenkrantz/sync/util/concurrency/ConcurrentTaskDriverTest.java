package com.github.trosenkrantz.sync.util.concurrency;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

class ConcurrentTaskDriverTest {
    private AsynchronousTask asynchronousTask;
    private List<Runnable> onDoneList;
    private ConcurrentTaskDriver driver;

    @BeforeEach
    void setUp() {
        asynchronousTask = onDone -> onDoneList.add(onDone);
        onDoneList = new ArrayList<>();
    }

    private void assertTasks(final int expectedQueued, final int expectedRunning, final int expectedFinished) {
        Assertions.assertEquals(expectedQueued, driver.getNumberOfQueuedTasks());
        Assertions.assertEquals(expectedRunning, driver.getNumberOfRunningTasks());
        Assertions.assertEquals(expectedFinished, driver.getNumberOfFinishedTasks());
    }

    private static void assertTasks(final TestListener listener, final int expectedQueued, final int expectedRunning, final int expectedFinished) {
        Assertions.assertEquals(expectedQueued, listener.getQueued());
        Assertions.assertEquals(expectedRunning, listener.getRunning());
        Assertions.assertEquals(expectedFinished, listener.getFinished());
    }

    private void finishAsynchronousTask() {
        onDoneList.remove(0).run();
    }

    @Test
    void max1RunningTask() {
        driver = new ConcurrentTaskDriver(1);

        driver.queueAsynchronous(Arrays.asList(asynchronousTask, asynchronousTask));
        assertTasks(1, 1, 0);

        finishAsynchronousTask();
        assertTasks(0, 1, 1);

        finishAsynchronousTask();
        assertTasks(0, 0, 2);
    }

    @Test
    void max2RunningTasks() {
        driver = new ConcurrentTaskDriver(2);

        driver.queueAsynchronous(Arrays.asList(asynchronousTask, asynchronousTask, asynchronousTask));
        assertTasks(1, 2, 0);

        finishAsynchronousTask();
        assertTasks(0, 2, 1);

        finishAsynchronousTask();
        assertTasks(0, 1, 2);

        finishAsynchronousTask();
        assertTasks(0, 0, 3);
    }

    @Test
    void noMaxRunningTasks() {
        driver = new ConcurrentTaskDriver();

        driver.queueAsynchronous(Arrays.asList(asynchronousTask, asynchronousTask));
        assertTasks(0, 2, 0);

        finishAsynchronousTask();
        assertTasks(0, 1, 1);

        finishAsynchronousTask();
        assertTasks(0, 0, 2);
    }

    @Test
    void increaseMaxRunningTasks() {
        driver = new ConcurrentTaskDriver(1);

        driver.queueAsynchronous(Arrays.asList(asynchronousTask, asynchronousTask));
        assertTasks(1, 1, 0);

        driver.setMaxRunningTasks(2);
        assertTasks(0, 2, 0);

        finishAsynchronousTask();
        assertTasks(0, 1, 1);

        finishAsynchronousTask();
        assertTasks(0, 0, 2);
    }

    @Test
    void reduceMaxRunningTasks() {
        driver = new ConcurrentTaskDriver(2);

        driver.queueAsynchronous(Arrays.asList(asynchronousTask, asynchronousTask, asynchronousTask));
        assertTasks(1, 2, 0);

        driver.setMaxRunningTasks(1);
        assertTasks(1, 2, 0);

        finishAsynchronousTask();
        assertTasks(1, 1, 1);

        finishAsynchronousTask();
        assertTasks(0, 1, 2);

        finishAsynchronousTask();
        assertTasks(0, 0, 3);
    }

    @Test
    void listener() {
        driver = new ConcurrentTaskDriver(1);
        TestListener listener = new TestListener();
        driver.addListener(listener);

        driver.queueAsynchronous(Arrays.asList(asynchronousTask, asynchronousTask));
        assertTasks(listener, 1, 1, 0);

        finishAsynchronousTask();
        assertTasks(listener, 0, 1, 1);

        driver.removeListener(listener);
        finishAsynchronousTask();
        assertTasks(listener, 0, 1, 1);
    }

    @Test
    void clearQueue() {
        driver = new ConcurrentTaskDriver(1);

        driver.queueAsynchronous(Arrays.asList(asynchronousTask, asynchronousTask));
        assertTasks(1, 1, 0);

        driver.clearQueue();
        assertTasks(0, 1, 0);

        finishAsynchronousTask();
        assertTasks(0, 0, 1);
    }

    @Test
    void queueSynchronousTask() {
        driver = new ConcurrentTaskDriver();

        driver.queue(
                () -> assertTasks(0, 1, 0)
        );
        assertTasks(0, 0, 1);
    }

    @Test
    void queueSynchronousTasksLimit1() {
        // Arrange
        driver = new ConcurrentTaskDriver(1);

        // Act
        driver.queueSynchronous(Arrays.asList(
                () -> assertTasks(1, 1, 0),
                () -> assertTasks(0, 1, 1)
        ));

        // Assert
        assertTasks(0, 0, 2);
    }

    @Test
    void queueSynchronousTasksNoLimit() {
        // Arrange
        driver = new ConcurrentTaskDriver();

        // Act
        driver.queueSynchronous(Arrays.asList(() -> { }, () -> { }));

        // Assert
        assertTasks(0, 0, 2);
    }

    @Test
    void suspend() {
        driver = new ConcurrentTaskDriver(2);
        driver.queueAsynchronous(Arrays.asList(asynchronousTask, asynchronousTask, asynchronousTask));

        driver.suspend();
        assertTasks(1, 2, 0);

        finishAsynchronousTask();
        assertTasks(1, 1, 1);

        finishAsynchronousTask();
        assertTasks(1, 0, 2);

        driver.resume();
        assertTasks(0, 1, 2);

        finishAsynchronousTask();
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

        finishAsynchronousTask();
        assertTasks(1, 1, 1);
        Assertions.assertEquals(0, whenIdleCalledCount[0]);

        finishAsynchronousTask();
        assertTasks(1, 0, 2);

        driver.resume();
        assertTasks(0, 1, 2);

        finishAsynchronousTask();
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
        finishAsynchronousTask();
        assertTasks(2, 0, 1);

        driver.resume();
        assertTasks(1, 1, 1);

        driver.suspend();
        assertTasks(1, 1, 1);

        finishAsynchronousTask();
        assertTasks(1, 0, 2);
    }

    @Test
    void exceptionPassesThroughAsynchronousTaskAndTaskIsNotDone() {
        driver = new ConcurrentTaskDriver();

        Assertions.assertThrows(RuntimeException.class, () ->
                driver.queue(onDone -> {
                    throw new RuntimeException();
                })
        );
        assertTasks(0, 1, 0);
    }

    @Test
    void exceptionPassesThroughSynchronousTaskAndTaskIsDone() {
        driver = new ConcurrentTaskDriver();

        Assertions.assertThrows(RuntimeException.class, () ->
                driver.queue(() -> {
                    throw new RuntimeException();
                })
        );
        assertTasks(0, 0, 1);
    }

    @Test
    void finishingAsynchronousTaskMultipleTimesHasNoEffect() {
        // Arrange
        driver = new ConcurrentTaskDriver();
        AtomicReference<Runnable> finish = new AtomicReference<>();

        // Act
        driver.queue(finish::set);
        finish.get().run();
        finish.get().run();
        finish.get().run();

        // Assert
        assertTasks(0, 0, 1);
    }
}