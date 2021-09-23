package com.exasol.matcher;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Date;
import java.sql.Timestamp;

import org.junit.jupiter.api.Test;

class FuzzyCellMatcherTest {

    @Test
    void testMatchDate() {
        assertTrue(FuzzyCellMatcher.fuzzilyEqualTo(new Date(1001)).matches(new Date(1001)));
    }

    @Test
    void testMatchSqlDateAndUtilDate() {
        assertTrue(FuzzyCellMatcher.fuzzilyEqualTo(new java.util.Date(1001)).matches(new Date(1001)));
    }

    @Test
    void testMatchDatesWithDifferentTimestampOnSameDay() {
        assertTrue(FuzzyCellMatcher.fuzzilyEqualTo(new java.util.Date(0)).matches(new Date(23 * 60 * 60 * 1000L)));
    }

    @Test
    void testMismatchDatesWithDifferentDays() {
        assertFalse(FuzzyCellMatcher.fuzzilyEqualTo(new java.util.Date(0)).matches(new Date(25 * 60 * 60 * 1000L)));
    }

    @Test
    void testMatchTimestampAndDate() {
        assertTrue(FuzzyCellMatcher.fuzzilyEqualTo(new Timestamp(1001)).matches(new Date(1001)));
    }

    @Test
    void testMismatchDate() {
        assertFalse(FuzzyCellMatcher.fuzzilyEqualTo(new Date(1001)).matches(new Date(100000000)));
    }

    @Test
    void testMismatchDateAndTimestamp() {
        assertFalse(FuzzyCellMatcher.fuzzilyEqualTo(new Date(1001)).matches(new Timestamp(1002)));
    }

    @Test
    void testMismatchDateAndString() {
        assertFalse(FuzzyCellMatcher.fuzzilyEqualTo(new Date(1001)).matches("other"));
    }

    @Test
    void testTimestampMismatchDescription() {
        final Timestamp timestamp1 = new Timestamp(1632320218000L);
        final Timestamp timestamp2 = new Timestamp(1632320518000L);
        final FuzzyCellMatcher<Timestamp> matcher = FuzzyCellMatcher.fuzzilyEqualTo(timestamp2);
        final AssertionError assertionError = assertThrows(AssertionError.class, () -> assertThat(timestamp1, matcher));
        assertThat(assertionError.getMessage(), equalTo(
                "\nExpected: a value equal to \"2021-09-22T14:21:58Z\"\n     but:  was \"2021-09-22T14:16:58Z\""));
    }

    @Test
    void testDateMismatchDescription() {
        final Date timestamp1 = new Date(1632320218000L);
        final Date timestamp2 = new Date(1632520218000L);
        final FuzzyCellMatcher<Date> matcher = FuzzyCellMatcher.fuzzilyEqualTo(timestamp2);
        final AssertionError assertionError = assertThrows(AssertionError.class, () -> assertThat(timestamp1, matcher));
        assertThat(assertionError.getMessage(),
                equalTo("\nExpected: a value equal to \"2021-09-24\"\n     but:  was \"2021-09-22\""));
    }
}