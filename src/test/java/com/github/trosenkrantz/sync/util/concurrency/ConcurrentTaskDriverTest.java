package com.github.trosenkrantz.sync.util.concurrency;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;

abstract class ConcurrentTaskDriverTest {
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
        Assertions.assertEquals(expectedQueued, driver.getNumberOfQueuedTasks(), "Wrong number of queued tasks");
        Assertions.assertEquals(expectedRunning, driver.getNumberOfRunningTasks(), "Wrong number of running tasks");
        Assertions.assertEquals(expectedFinished, driver.getNumberOfFinishedTasks(), "Wrong number of finished tasks");
    }

    @BeforeEach
    void setUp() {
        onDoneList = new ArrayList<>();
        asynchronousTask = onDoneList::add;
    }
}