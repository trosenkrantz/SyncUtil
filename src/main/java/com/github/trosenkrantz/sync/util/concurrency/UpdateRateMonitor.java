package com.github.trosenkrantz.sync.util.concurrency;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;
import java.util.function.Consumer;

/**
 * Tracks update frequencies within a sliding time window.
 * Adjusts the calculation during the initial ramp-up period to provide accurate rates.
 * @param <ID> The type of the identifier for the objects.
 */
public class UpdateRateMonitor<ID> {
    private final Map<ID, Queue<Instant>> updateHistory = new ConcurrentHashMap<>();
    private final Duration windowSize;
    private final AtomicReference<Instant> firstUpdateEver = new AtomicReference<>();
    private final Clock clock;
    private final List<Consumer<ID>> listeners = new CopyOnWriteArrayList<>();
    private int minUpdatesPerObjectForAverage = 2;

    public int getMinUpdatesPerObjectForAverage() {
        return minUpdatesPerObjectForAverage;
    }

    public void setMinUpdatesPerObjectForAverage(int minUpdates) {
        this.minUpdatesPerObjectForAverage = minUpdates;
    }

    public UpdateRateMonitor(Duration windowSize) {
        this(windowSize, Clock.systemUTC());
    }

    public UpdateRateMonitor(Duration windowSize, Clock clock) {
        this.windowSize = windowSize;
        this.clock = clock;
    }



    /**
     * Subscribes a listener to be notified when an update is recorded for any object.
     * The listener will be called synchronously within the thread that records the update.
     *
     * @param listener A consumer that will receive the ID of the updated object.
     */
    public void subscribe(Consumer<ID> listener) {
        listeners.add(listener);
    }

    /**
     * Unsubscribes a listener.
     *
     * @param listener The listener to remove.
     */
    public void unsubscribe(Consumer<ID> listener) {
        listeners.remove(listener);
    }

    private void trackFirstUpdate() {
        firstUpdateEver.compareAndSet(null, Instant.now(clock));
    }

    private void notifyListeners(ID id) {
        for (Consumer<ID> listener : listeners) {
            listener.accept(id);
        }
    }

    /**
     * Returns the duration to use as the divisor for rate calculations.
     * Prevents artificially low rates during the initial window filling period.
     */
    private double getEffectiveWindowSeconds(Instant now) {
        Instant start = firstUpdateEver.get();
        if (start == null) return windowSize.toMillis() / 1000.0;
        
        long millisSinceStart = Duration.between(start, now).toMillis();
        long effectiveMillis = Math.max(1, Math.min(millisSinceStart, windowSize.toMillis()));
        return effectiveMillis / 1000.0;
    }

    /**
     * Records an update and proactively prunes expired records for this ID.
     */
    public void recordUpdate(ID id) {
        trackFirstUpdate();
        Queue<Instant> timestamps = updateHistory.computeIfAbsent(id, inputID -> new ConcurrentLinkedQueue<>());

        Instant now = Instant.now(clock);
        timestamps.add(now);
        prune(id, timestamps, now);
        notifyListeners(id);
    }

    /**
     * Gets the global update rate across all tracked objects.
     *
     * @return the update rate in updates per second, or 0.0 if no updates have been recorded yet.
     */
    public double getGlobalUpdateRate() {
        Instant now = Instant.now(clock);
        long totalRecentUpdates = updateHistory.entrySet().stream()
            .mapToLong(entry -> {
                prune(entry.getKey(), entry.getValue(), now);
                return entry.getValue().size();
            })
            .sum();

        return (double) totalRecentUpdates / getEffectiveWindowSeconds(now);
    }

    /**
     * Gets the global average update rate per object, considering objects
     * that have been updated at least minUpdatesPerObjectForAverage times.
     * This prevents artificially high averages when objects are first being tracked.
     * The rate is in updates per second.
     *
     * @return the average update rate in updates per second, or empty Optional if no objects meet the threshold.
     */
    public Optional<Double> getAverageUpdateRatePerObject() {
        Instant now = Instant.now(clock);
        double effectiveWindowSeconds = getEffectiveWindowSeconds(now);
        
        List<Double> rates = updateHistory.values().stream()
            .filter(instants -> instants.size() >= minUpdatesPerObjectForAverage)
            .map(instants -> (double) instants.size() / effectiveWindowSeconds)
            .collect(Collectors.toList());

        if (rates.isEmpty()) {
            return Optional.empty();
        }

        double average = rates.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);

        return Optional.of(average);
    }

    public double getAverageUpdateRateById(ID id) {
        Queue<Instant> timestamps = updateHistory.get(id);
        if (timestamps == null) return 0.0;

        Instant now = Instant.now(clock);
        prune(id, timestamps, now);
        
        return (double) timestamps.size() / getEffectiveWindowSeconds(now);
    }

    /**
     * Removes timestamps outside the sliding window.
     * If no timestamps remain, the ID is removed from the tracking map.
     */
    private void prune(ID id, Queue<Instant> timestamps, Instant now) {
        Instant threshold = now.minus(windowSize);
        timestamps.removeIf(t -> t.isBefore(threshold));
        if (timestamps.isEmpty()) {
            updateHistory.remove(id);
        }
    }
}
