package com.github.trosenkrantz.sync.util.concurrency;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LimitTest {
    @Test
    void getLimit() {
        Assertions.assertEquals(2, Limit.of(2).get());
    }

    @Test
    void cannotGetLimitlessValue() {
        Assertions.assertThrows(Exception.class, () -> Limit.noLimit().get());
    }

    @Test
    void cannotCreateZeroLimitLimit() {
        Assertions.assertThrows(Exception.class, () -> Limit.of(0));
    }

    @Test
    void cannotCreateNegativeLimitLimit() {
        Assertions.assertThrows(Exception.class, () -> Limit.of(-1));
    }

    @Test
    void aLimitHasLimit() {
        Assertions.assertTrue(Limit.of(2).hasLimit());
    }

    @Test
    void limitLessHasNoLimit() {
        Assertions.assertFalse(Limit.noLimit().hasLimit());
    }

    @Test
    void concreteLimitIsGreaterThan() {
        Assertions.assertTrue(Limit.of(2).isGreaterThan(1));
        Assertions.assertFalse(Limit.of(2).isGreaterThan(2));
        Assertions.assertFalse(Limit.of(2).isGreaterThan(3));
    }

    @Test
    void limitlessIsGreaterThan() {
        Assertions.assertTrue(Limit.noLimit().isGreaterThan(1));
        Assertions.assertTrue(Limit.noLimit().isGreaterThan(2));
        Assertions.assertTrue(Limit.noLimit().isGreaterThan(3));
    }

    @Test
    void concreteLimitIsLessThan() {
        Assertions.assertFalse(Limit.of(2).isLessThan(1));
        Assertions.assertFalse(Limit.of(2).isLessThan(2));
        Assertions.assertTrue(Limit.of(2).isLessThan(3));
    }

    @Test
    void limitlessIsLessThan() {
        Assertions.assertFalse(Limit.noLimit().isLessThan(1));
        Assertions.assertFalse(Limit.noLimit().isLessThan(2));
        Assertions.assertFalse(Limit.noLimit().isLessThan(3));
    }

    @Test
    void concreteLimitIsGreaterThanOrEquals() {
        Assertions.assertTrue(Limit.of(2).isGreaterThanOrEquals(1));
        Assertions.assertTrue(Limit.of(2).isGreaterThanOrEquals(2));
        Assertions.assertFalse(Limit.of(2).isGreaterThanOrEquals(3));
    }

    @Test
    void limitlessIsGreaterThanOrEquals() {
        Assertions.assertTrue(Limit.noLimit().isGreaterThanOrEquals(1));
        Assertions.assertTrue(Limit.noLimit().isGreaterThanOrEquals(2));
        Assertions.assertTrue(Limit.noLimit().isGreaterThanOrEquals(3));
    }

    @Test
    void concreteLimitIsLessThanOrEquals() {
        Assertions.assertFalse(Limit.of(2).isLessThanOrEquals(1));
        Assertions.assertTrue(Limit.of(2).isLessThanOrEquals(2));
        Assertions.assertTrue(Limit.of(2).isLessThanOrEquals(3));
    }

    @Test
    void limitlessIsLessThanOrEquals() {
        Assertions.assertFalse(Limit.noLimit().isLessThanOrEquals(1));
        Assertions.assertFalse(Limit.noLimit().isLessThanOrEquals(2));
        Assertions.assertFalse(Limit.noLimit().isLessThanOrEquals(3));
    }
}