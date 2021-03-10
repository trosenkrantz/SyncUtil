package com.github.trosenkrantz.sync.util.concurrency;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

class ConcurrentTaskDriverTest {
    protected ConcurrentTaskDriver driver;
    protected AsynchronousTask asynchronousTask;

    private List<Runnable> onDoneList;

    public void finishTask() {
        onDoneList.remove(0).run();
    }
    
    public static void assertTasks(final TestListener listener, final int expectedQueued, final int expectedRunning, final int expectedFinished) {
        Assertions.assertEquals(expectedQueued, listener.getQueued());
        Assertions.assertEquals(expectedRunning, listener.getRunning());
        Assertions.assertEquals(expectedFinished, listener.getFinished());
    }

    public void assertTasks(final int expectedQueued, final int expectedRunning, final int expectedFinished) {
        Assertions.assertEquals(expectedQueued, driver.getNumberOfQueuedTasks());
        Assertions.assertEquals(expectedRunning, driver.getNumberOfRunningTasks());
        Assertions.assertEquals(expectedFinished, driver.getNumberOfFinishedTasks());
    }

    @BeforeEach
    void setUp() {
        onDoneList = new ArrayList<>();
        asynchronousTask = onDoneList::add;
    }
}