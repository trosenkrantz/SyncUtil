package com.github.trosenkrantz.sync.util.runnable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.OngoingStubbing;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class SynchronisedSchedulerTest {
    private ScheduledExecutorService executor;
    private SynchronisedScheduler scheduler;

    @BeforeEach
    void setUp() {
        executor = mock(ScheduledExecutorService.class);
        scheduler = new SynchronisedScheduler(executor);
    }

    @Test
    void scheduleAndRun() {
        Runnable task = mock(Runnable.class);

        ScheduledFuture<?> mockFuture = mock(ScheduledFuture.class);
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);

        doReturn(mockFuture).when(executor).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));

        ScheduledFuture<?> unused = scheduler.schedule(task, 10, TimeUnit.SECONDS);

        verify(executor).schedule(captor.capture(), eq(10L), eq(TimeUnit.SECONDS));

        captor.getValue().run();

        verify(task).run();
    }

    @Test
    void scheduleOnInactivityAndRun() {
        Runnable task = mock(Runnable.class);
        ScheduledFuture<?> mockFuture = mock(ScheduledFuture.class);
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);

        doReturn(mockFuture).when(executor).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));

        scheduler.scheduleOnInactivity(task, 5, TimeUnit.MINUTES);

        verify(executor).schedule(captor.capture(), eq(5L), eq(TimeUnit.MINUTES));

        captor.getValue().run();

        verify(task).run();
    }

    @Test
    void nextWindowCancelsPending() {
        Runnable task = mock(Runnable.class);
        ScheduledFuture<?> mockFuture = mock(ScheduledFuture.class);
        doReturn(mockFuture).when(executor).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));

        // Schedule a task but do not run it yet
        ScheduledFuture<?> unused = scheduler.schedule(task, 10, TimeUnit.SECONDS);

        // Act: Transition to the next window
        scheduler.nextWindow();

        // Assert: The pending future from the previous window must be cancelled
        verify(mockFuture).cancel(false);

        // Attempt to run the captured task from the old window should now do nothing
        // because the window was closed / transitioned
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).schedule(captor.capture(), anyLong(), any(TimeUnit.class));
        captor.getValue().run();

        verify(task, never()).run();
    }

    @Test
    void markActivityReschedulesInactivity() {
        Runnable task = mock(Runnable.class);
        ScheduledFuture<?> mockFuture = mock(ScheduledFuture.class);
        doReturn(mockFuture).when(executor).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));

        scheduler.scheduleOnInactivity(task, 5, TimeUnit.MINUTES);

        // Initial schedule on creation
        verify(executor).schedule(any(Runnable.class), eq(5L), eq(TimeUnit.MINUTES));

        // Act: Mark activity to reset the timer
        scheduler.markActivity();

        // Assert: Previous timer should be cancelled and a new one scheduled
        verify(mockFuture).cancel(false);
        verify(executor, times(2)).schedule(any(Runnable.class), eq(5L), eq(TimeUnit.MINUTES));
    }

    @Test
    void closeAndMarkActivity() {
        Runnable task = mock(Runnable.class);
        ScheduledFuture<?> mockFuture = mock(ScheduledFuture.class);
        doReturn(mockFuture).when(executor).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));

        // Arrange: Set up inactivity tracking
        scheduler.scheduleOnInactivity(task, 5, TimeUnit.MINUTES);
        verify(executor).schedule(any(Runnable.class), eq(5L), eq(TimeUnit.MINUTES));

        // Act: Close the scheduler
        scheduler.close(false);
        verify(mockFuture).cancel(false);

        // Reset the mock to clearly see if any new interactions occur
        reset(executor);

        // Act: Try to mark activity after closure
        scheduler.markActivity();

        // Assert: No new schedules should be created
        verifyNoInteractions(executor);
    }
}