package com.github.trosenkrantz.sync.util.concurrency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.ExecutorService;

class ExecutorServiceTaskTest extends ConcurrentTaskDriverTest {
    private ExecutorService executor;

    private int runCount;

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
        assertTasks(0, 0, 1);
    }

    @Test
    void runSameTaskTwice() {
        driver = new ConcurrentTaskDriver();

        ExecutorServiceTask task = new ExecutorServiceTask(() -> runCount++, executor);
        driver.queue(task);
        assertTasks(0, 0, 1);
        driver.queue(task);
        assertTasks(0, 0, 2);
    }
}
