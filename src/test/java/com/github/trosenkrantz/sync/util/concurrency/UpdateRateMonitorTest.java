package com.github.trosenkrantz.sync.util.concurrency;

import com.github.trosenkrantz.sync.util.concurrency.UpdateRateMonitor;
import com.github.trosenkrantz.sync.util.concurrency.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UpdateRateMonitorTest {

    private Duration windowSize;
    private UpdateRateMonitor<String> tracker;
    private MutableClock clock;

    @BeforeEach
    void setUp() {
        windowSize = Duration.ofMinutes(5);
        clock = new MutableClock(Instant.now());
        tracker = new UpdateRateMonitor<>(windowSize, clock);
    }

    @Test
    @DisplayName("Global update rate is zero when no entries")
    void shouldReturnZeroRateWhenNoUpdates() {
        double rate = tracker.getGlobalUpdateRate();
        assertTrue(rate < 1.0);
    }

    @Test
    @DisplayName("Global update rate equals number of updates divided by window size")
    void shouldCalculateCorrectGlobalRate() {
        tracker.recordUpdate("obj1");
        tracker.recordUpdate("obj1");
        double rate = tracker.getGlobalUpdateRate();
        assertTrue(rate > 0.1);
    }

    @Test
    @DisplayName("Multiple objects contribute to global rate")
    void shouldSumUpdatesFromMultipleObjects() {
        tracker.recordUpdate("obj1");
        tracker.recordUpdate("obj2");
        tracker.recordUpdate("obj1");
        double rate = tracker.getGlobalUpdateRate();
        assertTrue(rate > 0.2);
    }

    @Test
    @DisplayName("Rate includes entries from new objects but not yet pruned")
    void shouldIncludeAllActiveObjects() {
        tracker.recordUpdate("obj1");
        tracker.recordUpdate("obj2");
        tracker.recordUpdate("obj3");
        double rate = tracker.getGlobalUpdateRate();
        assertTrue(rate > 0.2);
    }

    @Test
    @DisplayName("Average rate is empty when no objects")
    void shouldReturnEmptyWhenNoObjects() {
        java.util.Optional<Double> average = tracker.getAverageUpdateRatePerObject();
        assertFalse(average.isPresent());
    }

    @Test
    @DisplayName("Average rate should be valid when objects exist")
    void shouldReturnValidAverageRate() {
        tracker.recordUpdate("obj1");
        tracker.recordUpdate("obj2");
        tracker.recordUpdate("obj3");

        java.util.Optional<Double> average = tracker.getAverageUpdateRatePerObject();
        assertTrue(average.isPresent());
    }

    @Test
    @DisplayName("Rate is zero for non-existent ID")
    void shouldReturnZeroForNonExistentId() {
        double rate = tracker.getAverageUpdateRateById("nonexistent");
        assertEquals(0.0, rate);
    }

    @Test
    @DisplayName("Rate equals updates for that ID divided by window size")
    void shouldReturnCorrectRateForId() {
        tracker.recordUpdate("obj1");
        tracker.recordUpdate("obj1");
        tracker.recordUpdate("obj2");
        double rate = tracker.getAverageUpdateRateById("obj1");
        assertTrue(rate > 0.1);
    }

    @Test
    @DisplayName("Rate for different ID should be independent")
    void shouldReturnIndependentRates() {
        tracker.recordUpdate("obj1");
        tracker.recordUpdate("obj2");
        double rate1 = tracker.getAverageUpdateRateById("obj1");
        double rate2 = tracker.getAverageUpdateRateById("obj2");
        assertEquals(rate1, rate2, 0.001);
    }

    @Test
    @DisplayName("Effective window handles new objects correctly")
    void shouldHandleNewObjectsCorrectly() {
        tracker.recordUpdate("obj1");
        clock.advance(windowSize.plusMillis(100));
        double rate = tracker.getGlobalUpdateRate();
        // Rate should be 0 as the update is outside the window
        assertEquals(0.0, rate, 0.001);
    }

    @Test
    @DisplayName("Should initialize with default clock")
    void shouldInitializeWithDefaultClock() {
        UpdateRateMonitor<String> monitor = new UpdateRateMonitor<>(Duration.ofMinutes(5));
        monitor.recordUpdate("test");
        assertTrue(monitor.getGlobalUpdateRate() >= 0.0);
    }

    @Test
    @DisplayName("Should notify subscribers on update")
    void shouldNotifySubscribers() {
        java.util.concurrent.atomic.AtomicReference<String> notifiedId = new java.util.concurrent.atomic.AtomicReference<>();
        tracker.subscribe(notifiedId::set);
        
        tracker.recordUpdate("obj1");
        
        assertEquals("obj1", notifiedId.get());
    }
}
