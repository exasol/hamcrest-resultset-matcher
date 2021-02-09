package com.exasol.matcher;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Date;
import java.sql.Timestamp;

import org.junit.jupiter.api.Test;

class FuzzyCellMatcherTest {

    @Test
    void testMatchDate() {
        assertTrue(FuzzyCellMatcher.fuzzilyEqualTo(new Date(1001)).matches(new Date(1001)));
    }

    @Test
    void testMatchDateAndTimestamp() {
        assertTrue(FuzzyCellMatcher.fuzzilyEqualTo(new Date(1001)).matches(new Timestamp(1001)));
    }

    @Test
    void testMatchTimestampAndDate() {
        assertTrue(FuzzyCellMatcher.fuzzilyEqualTo(new Timestamp(1001)).matches(new Date(1001)));
    }

    @Test
    void testMismatchDate() {
        assertFalse(FuzzyCellMatcher.fuzzilyEqualTo(new Date(1001)).matches(new Date(1002)));
    }

    @Test
    void testMismatchDateAndTimestamp() {
        assertFalse(FuzzyCellMatcher.fuzzilyEqualTo(new Date(1001)).matches(new Timestamp(1002)));
    }

    @Test
    void testMismatchDateAndString() {
        assertFalse(FuzzyCellMatcher.fuzzilyEqualTo(new Date(1001)).matches("other"));
    }
}