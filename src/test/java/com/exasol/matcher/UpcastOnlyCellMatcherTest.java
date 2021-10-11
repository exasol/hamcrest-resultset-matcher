package com.exasol.matcher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

class UpcastOnlyCellMatcherTest {
    @Test
    void testNullMatch() {
        final Integer nullValue = null;
        assertThat(nullValue, UpcastOnlyCellMatcher.isOnlyUpcastTo(nullValue));
    }

    @Test
    void testUpcastMatch() {
        final Short actual = 500;
        final Long expected = 500L;
        assertThat(actual, UpcastOnlyCellMatcher.isOnlyUpcastTo(expected));
    }

    @Test
    void testDowncastMismatch() {
        final Long actual = 500L;
        final Short expected = 500;
        assertThat(UpcastOnlyCellMatcher.isOnlyUpcastTo(expected).matches(actual), equalTo(false));
    }
}