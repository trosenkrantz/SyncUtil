package com.github.trosenkrantz.sync.util.concurrency;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.ExecutorService;

class ExecutorServiceTaskTest extends ConcurrentTaskDriverTest {
    private int runCount;
    private ExecutorService executor;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();

        runCount = 0;
        executor = Mockito.mock(ExecutorService.class);
        Mockito.doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(this.executor).execute(Mockito.any());
    }

    @Test
    void runTask() {
        driver = new ConcurrentTaskDriver();

        driver.queue(new ExecutorServiceTask(() -> runCount++, executor));
        Assertions.assertEquals(1, runCount);
        assertTasks(0, 0, 1);
    }

    @Test
    void runSameTaskTwice() {
        driver = new ConcurrentTaskDriver();

        ExecutorServiceTask task = new ExecutorServiceTask(() -> runCount++, executor);
        driver.queue(task);
        Assertions.assertEquals(1, runCount);
        assertTasks(0, 0, 1);
        driver.queue(task);
        Assertions.assertEquals(2, runCount);
        assertTasks(0, 0, 2);
    }
}
