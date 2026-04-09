package com.github.trosenkrantz.sync.util.runnable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SingleRunnableTest {
    private int count;

    private void runnable() {
        count++;
    }

    @BeforeEach
    void setUp() {
        count = 0;
    }

    @Test
    void run0Times() {
        new SingleRunnable(this::runnable);

        assertEquals(0, count);
    }

    @Test
    void run1Time() {
        SingleRunnable singleRunnable = new SingleRunnable(this::runnable);

        singleRunnable.run();

        assertEquals(1, count);
    }

    @Test
    void run2Times() {
        SingleRunnable singleRunnable = new SingleRunnable(this::runnable);

        singleRunnable.run();
        singleRunnable.run();

        assertEquals(1, count);
    }

    @Test
    void run100Times() {
        SingleRunnable singleRunnable = new SingleRunnable(this::runnable);

        for (int i = 0; i < 100; i++) {
            singleRunnable.run();
        }

        assertEquals(1, count);
    }
}