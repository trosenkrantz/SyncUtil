package com.github.trosenkrantz.sync.util.runnable;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class SingleRunnableManagerTest {
    private int count1;
    private int count2;

    private final Runnable runnable1 = () -> count1++;
    private final Runnable runnable2 = () -> count2++;

    @BeforeEach
    void setUp() {
        count1 = 0;
        count2 = 0;
    }

    @Test
    void runSameRunnableTwoTimes() {
        // Arrange
        SingleRunnableManager manager = new SingleRunnableManager();

        // Act
        manager.run(runnable1);
        manager.run(runnable1);

        // Assert
        assertEquals(1, count1);
    }

    @Test
    void runTwoRunnable() {
        // Arrange
        SingleRunnableManager manager = new SingleRunnableManager();

        // Act
        manager.run(runnable1);
        manager.run(runnable2);

        // Assert
        assertEquals(1, count1);
        assertEquals(0, count2);
    }

    @Test
    void runTwoRunnableMultipleTimes() {
        // Arrange
        SingleRunnableManager manager = new SingleRunnableManager();

        // Act
        manager.run(runnable1);
        manager.run(runnable2);
        manager.run(runnable1);
        manager.run(runnable2);
        manager.run(runnable1);
        manager.run(runnable2);

        // Assert
        assertEquals(1, count1);
        assertEquals(0, count2);
    }

    @Test
    void scheduleWhileSuspended() {
        // Arrange
        SingleRunnableManager manager = new SingleRunnableManager(false);

        // Act
        manager.run(runnable1);
        assertEquals(0, count1);
        manager.allow();

        // Assert
        assertEquals(1, count1);
    }

    @Test
    void allowWhileIdle() {
        // Arrange
        SingleRunnableManager manager = new SingleRunnableManager();

        // Act
        manager.allow();
        manager.run(runnable1);

        // Assert
        assertEquals(1, count1);
    }

    @Test
    void allowAfterSuspended() {
        // Arrange
        SingleRunnableManager manager = new SingleRunnableManager(false);

        // Act
        manager.allow();
        manager.run(runnable1);

        // Assert
        assertEquals(1, count1);
    }

    @Test
    void scheduleWhenSuspended() {
        SingleRunnableManager manager = new SingleRunnableManager(true);

        manager.suspend();
        manager.run(runnable1); // Scheduled to be run
        assertEquals(0, count1);

        manager.allow(); // Now it runs
        assertEquals(1, count1);

        // Do some shenanigans and assert it has no effect
        manager.run(runnable1);
        manager.allow();
        manager.run(runnable1);
        assertEquals(1, count1);
    }

    @Test
    void suspendWhileSuspended() {
        SingleRunnableManager manager = new SingleRunnableManager(false);

        manager.suspend(); // Must have no effect
        manager.run(runnable1);
        assertEquals(0, count1);

        manager.allow(); // Now it runs
        assertEquals(1, count1);
    }

    @Test
    void suspendAfterAlreadyRun() {
        SingleRunnableManager manager = new SingleRunnableManager();
        manager.run(runnable1);

        manager.suspend();
        manager.allow();
        manager.run(runnable1);
        assertEquals(1, count1); // Still only run once
    }

    @Test
    void suspendWhileScheduled() {
        SingleRunnableManager manager = new SingleRunnableManager(false);
        manager.run(runnable1); // Schedule

        manager.suspend(); // Must have no effect
        assertEquals(0, count1);
        manager.allow(); // Now run
        assertEquals(1, count1);
    }

    @Test
    void tryToRunSameRunnableAfterAlreadyScheduled() {
        SingleRunnableManager manager = new SingleRunnableManager(true);

        manager.suspend();
        manager.run(runnable1); // Scheduled to be run
        assertEquals(0, count1);

        // Trying to run again must be ignored
        manager.run(runnable1);
        assertEquals(0, count1);

        manager.allow(); // Now it runs, but only once
        assertEquals(1, count1);
    }

    @Test
    void tryToRunOtherRunnableAfterAlreadyScheduled() {
        SingleRunnableManager manager = new SingleRunnableManager(true);

        manager.suspend();
        manager.run(runnable1); // Scheduled to be run
        assertEquals(0, count1);

        // Trying to run again must be ignored
        manager.run(runnable2);
        assertEquals(0, count1);
        assertEquals(0, count2);

        manager.allow(); // Now it runs, but only the first runnable
        assertEquals(1, count1);
        assertEquals(0, count2);
    }

    @Test
    void wrap() {
        SingleRunnableManager manager = new SingleRunnableManager();
        Runnable wrapped = manager.wrap(runnable1);
        assertEquals(0, count1);

        wrapped.run();
        assertEquals(1, count1);

        manager.run(wrapped);
        manager.run(runnable2);
        assertEquals(1, count1);
        assertEquals(0, count2);
    }

    @Test
    void catchUnexpectedStateValue() throws NoSuchFieldException, IllegalAccessException {
        SingleRunnableManager manager = new SingleRunnableManager();

        Field stateField = SingleRunnableManager.class.getDeclaredField("state");
        stateField.setAccessible(true);
        stateField.set(manager, RunnableState.NEW_VALUE);

        Assertions.assertThrows(Exception.class, () -> manager.run(runnable1));
        Assertions.assertThrows(Exception.class, manager::allow);
        Assertions.assertThrows(Exception.class, manager::suspend);
    }
}