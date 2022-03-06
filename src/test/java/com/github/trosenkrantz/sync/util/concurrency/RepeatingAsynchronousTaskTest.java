package com.github.trosenkrantz.sync.util.concurrency;

import com.github.trosenkrantz.sync.util.runnable.SingleRunnable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class RepeatingAsynchronousTaskTest {
    private boolean outerDoneCalled;
    private List<SingleRunnable> innerDoneList;

    @BeforeEach
    void setUp() {
        outerDoneCalled = false;
        innerDoneList = new ArrayList<>();
    }

    @Test
    void limitless() {
        RepeatingAsynchronousTask taskUnderTest = new RepeatingAsynchronousTask(innerDoneList::add);

        taskUnderTest.run(new SingleRunnable(() -> outerDoneCalled = true)); // Start
        Assertions.assertEquals(1, innerDoneList.size()); // Inner task is run 1 time

        innerDoneList.get(0).run(); // Finish 1st time
        Assertions.assertEquals(2, innerDoneList.size()); // Inner task is run again

        innerDoneList.get(1).run(); // Finish 2nd time
        Assertions.assertEquals(3, innerDoneList.size()); // Inner task is run again
        Assertions.assertFalse(outerDoneCalled); // Outer task is still ongoing

        taskUnderTest.stop();
        innerDoneList.get(2).run(); // Finish 3rd time
        Assertions.assertEquals(3, innerDoneList.size()); // Inner task is NOT run again
        Assertions.assertTrue(outerDoneCalled); // Outer task is done
    }

    @Test
    void stoppedBeforeLimit() {
        RepeatingAsynchronousTask taskUnderTest = new RepeatingAsynchronousTask(innerDoneList::add, 4);

        taskUnderTest.run(new SingleRunnable(() -> outerDoneCalled = true)); // Start
        Assertions.assertEquals(1, innerDoneList.size()); // Inner task is run 1 time

        innerDoneList.get(0).run(); // Finish 1st time
        Assertions.assertEquals(2, innerDoneList.size()); // Inner task is run again

        innerDoneList.get(1).run(); // Finish 2nd time
        Assertions.assertEquals(3, innerDoneList.size()); // Inner task is run again
        Assertions.assertFalse(outerDoneCalled); // Outer task is still ongoing

        taskUnderTest.stop();
        innerDoneList.get(2).run(); // Finish 3rd time
        Assertions.assertEquals(3, innerDoneList.size()); // Inner task is NOT run again
        Assertions.assertTrue(outerDoneCalled); // Outer task is done
    }

    @Test
    void stoppedByLimit() {
        RepeatingAsynchronousTask taskUnderTest = new RepeatingAsynchronousTask(innerDoneList::add, 3);

        taskUnderTest.run(new SingleRunnable(() -> outerDoneCalled = true)); // Start
        Assertions.assertEquals(1, innerDoneList.size()); // Inner task is run 1 time

        innerDoneList.get(0).run(); // Finish 1st time
        Assertions.assertEquals(2, innerDoneList.size()); // Inner task is run again

        innerDoneList.get(1).run(); // Finish 2nd time
        Assertions.assertEquals(3, innerDoneList.size()); // Inner task is run again
        Assertions.assertFalse(outerDoneCalled); // Outer task is still ongoing

        innerDoneList.get(2).run(); // Finish 3rd time
        Assertions.assertEquals(3, innerDoneList.size()); // Inner task is NOT run again
        Assertions.assertTrue(outerDoneCalled); // Outer task is done
    }

    @Test
    void stoppedOnLimit() {
        RepeatingAsynchronousTask taskUnderTest = new RepeatingAsynchronousTask(innerDoneList::add, 3);

        taskUnderTest.run(new SingleRunnable(() -> outerDoneCalled = true)); // Start
        Assertions.assertEquals(1, innerDoneList.size()); // Inner task is run 1 time

        innerDoneList.get(0).run(); // Finish 1st time
        Assertions.assertEquals(2, innerDoneList.size()); // Inner task is run again

        innerDoneList.get(1).run(); // Finish 2nd time
        Assertions.assertEquals(3, innerDoneList.size()); // Inner task is run again
        Assertions.assertFalse(outerDoneCalled); // Outer task is still ongoing

        taskUnderTest.stop();
        innerDoneList.get(2).run(); // Finish 3rd time
        Assertions.assertEquals(3, innerDoneList.size()); // Inner task is NOT run again
        Assertions.assertTrue(outerDoneCalled); // Outer task is done
    }
}
