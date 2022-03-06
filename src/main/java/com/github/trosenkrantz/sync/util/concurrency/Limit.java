package com.github.trosenkrantz.sync.util.concurrency;

import java.util.NoSuchElementException;

/**
 * A representation of some positive limit, or a limitless representation.
 */
public class Limit {
    private final Integer value; // Null means no limit

    private Limit(final Integer value) {
        this.value = value;
    }

    /**
     * Creates a positive limit.
     * @param value value of limit
     * @return the limit
     * @throws IllegalArgumentException if value is non-positive
     */
    public static Limit of(final int value) throws IllegalArgumentException {
        if (value < 1) throw new IllegalArgumentException("Limit must be positive, but was " + value + ".");

        return new Limit(value);
    }

    /**
     * Creates a limitless representation.
     * @return the limit
     */
    public static Limit noLimit() {
        return new Limit(null);
    }

    /**
     * Gets if this is a concrete limit (not limitless).
     * @return true iff a concrete limit
     */
    public boolean hasLimit() {
        return value != null;
    }

    /**
     * Gets the concrete limit, if present.
     * @return the limit value
     * @throws NoSuchElementException if this is limitless
     */
    public Integer get() throws NoSuchElementException {
        if (value == null) throw new NoSuchElementException("There is No limit.");
        return value;
    }

    /**
     * Gets if this limit is greater than a specified value.
     * If this is limitless, this always returns true.
     * @param value the specified value
     * @return true iff greater than
     */
    public boolean isGreaterThan(final int value) {
        return this.value == null || this.value > value;
    }
}
