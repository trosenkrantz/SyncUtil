package com.github.trosenkrantz.sync.util.runnalbe;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    void run1Time() {
        SingleRunnableManager manager = new SingleRunnableManager();

        manager.run(runnable1);

        assertEquals(1, count1);
    }

    @Test
    void run2Times() {
        SingleRunnableManager manager = new SingleRunnableManager();

        manager.run(runnable1);
        manager.run(runnable1);

        assertEquals(1, count1);
    }

    @Test
    void runBoth() {
        SingleRunnableManager manager = new SingleRunnableManager();

        manager.run(runnable1);
        manager.run(runnable2);

        assertEquals(1, count1);
        assertEquals(0, count2);
    }

    @Test
    void runBothMultipleTimes() {
        SingleRunnableManager manager = new SingleRunnableManager();

        manager.run(runnable1);
        manager.run(runnable2);
        manager.run(runnable1);
        manager.run(runnable2);
        manager.run(runnable1);
        manager.run(runnable2);

        assertEquals(1, count1);
        assertEquals(0, count2);
    }

    @Test
    void suspendedFromStart() {
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
    void suspendedAfterStart() {
        // Arrange
        SingleRunnableManager manager = new SingleRunnableManager(true);

        // Act
        manager.suspend();
        manager.run(runnable1);
        assertEquals(0, count1);

        manager.allow();
        assertEquals(1, count1);

        manager.run(runnable1);
        manager.allow();
        manager.run(runnable1);

        // Assert
        assertEquals(1, count1);
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

}