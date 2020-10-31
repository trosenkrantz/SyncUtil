package com.github.trosenkrantz.sync.util.concurrency;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class ConcurrentTaskDriverLoadTest {
    @Test
    void testLoad() {
        load(100000, 100000);
    }

    private void load(final int size, final int iterations) {
        ConcurrentTaskDriver driver = new ConcurrentTaskDriver(4);
        AsynchronousTaskTestHelper TestHelper = new AsynchronousTaskTestHelper();
        long start = System.currentTimeMillis();

        for (int i = 0; i < size; i++) {
            driver.queue(TestHelper.getTask());
        }

        for (int i = 0; i < iterations; i++) {
            driver.queue(TestHelper.getTask());
            TestHelper.finishTask();
        }

        for (int i = 0; i < size; i++) {
            TestHelper.finishTask();
        }

        System.out.println("Size = " + size + ", Iterations = " + iterations + ", time = " + (System.currentTimeMillis() - start) + " ms.");
    }
}
