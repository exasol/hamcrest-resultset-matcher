package com.exasol.matcher;

import java.math.BigDecimal;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * Fuzzy matcher that matches cell contents with expected contents.
 * <p>
 * If the matcher knows a conversion between expected and actual type, it applies that conversion and tries to match the
 * values after that conversion.
 * </p>
 *
 * @param <T> Matched type
 */
public class FuzzyCellMatcher<T> extends BaseMatcher<T> {
    private final T expected;
    private final Class<?> expectedClass;

    /**
     * Creates a {@link FuzzyCellMatcher} that matches cell values in a more relaxed fashion.
     *
     * @param <T>      the expected type
     * @param expected expected value
     * @return new matcher instance
     */
    public static <T> FuzzyCellMatcher<T> fuzzilyEqualTo(final T expected) {
        return new FuzzyCellMatcher<>(expected);
    }

    /**
     * Create a new instance of a {@link FuzzyCellMatcher}.
     *
     * @param expected the expected cell content.
     */
    public FuzzyCellMatcher(final T expected) {
        this.expected = expected;
        this.expectedClass = expected.getClass();
    }

    @Override
    public boolean matches(final Object actual) {
        if (actual instanceof Number && this.expected instanceof Number) {
            return new BigDecimal(actual.toString()).compareTo(new BigDecimal(this.expected.toString())) == 0;
        } else {
            return actual.equals(this.expected);
        }
    }

    @Override
    public void describeTo(final Description description) {
        description.appendValue(this.expected) //
                .appendText(" (") //
                .appendText(this.expectedClass.getName()) //
                .appendText(")");
    }

    @Override
    public void describeMismatch(final Object item, final Description description) {
        description.appendValue(this.expected) //
                .appendText(" (") //
                .appendText(this.expectedClass.getName()) //
                .appendText(") was ") //
                .appendValue(item) //
                .appendText(" (") //
                .appendText(item.getClass().getName()) //
                .appendText(")");
    }
}