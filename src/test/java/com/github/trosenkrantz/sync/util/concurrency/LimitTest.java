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
    void aLimitHasLimit() {
        Assertions.assertTrue(Limit.of(2).hasLimit());
    }

    @Test
    void limitLessHasNoLimit() {
        Assertions.assertFalse(Limit.noLimit().hasLimit());
    }

    @Test
    void isGreaterThanConcreteLimit() {
        Assertions.assertTrue(Limit.of(2).isGreaterThan(1));
        Assertions.assertFalse(Limit.of(2).isGreaterThan(2));
        Assertions.assertFalse(Limit.of(2).isGreaterThan(3));
    }

    @Test
    void isGreaterThanLimitless() {
        Assertions.assertTrue(Limit.noLimit().isGreaterThan(1));
        Assertions.assertTrue(Limit.noLimit().isGreaterThan(2));
        Assertions.assertTrue(Limit.noLimit().isGreaterThan(3));
    }
}