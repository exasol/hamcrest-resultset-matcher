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
        final Class<?> actualClass = actual.getClass();
        if (actualClass.equals(this.expectedClass)) {
            return actual.equals(this.expected);
        } else if (actualClass.equals(java.math.BigDecimal.class)) {
            return matchBigDecimal(actual, this.expectedClass);
        } else {
            return actual.equals(this.expected);
        }
    }

    private boolean matchBigDecimal(final Object actual, final Class<?> expectedClass) {
        final BigDecimal concreteActual = (BigDecimal) actual;
        if (expectedClass.equals(java.lang.Byte.class)) {
            return concreteActual.equals(BigDecimal.valueOf((Byte) this.expected));
        } else if (expectedClass.equals(java.lang.Short.class)) {
            return concreteActual.equals(BigDecimal.valueOf((Short) this.expected));
        } else if (expectedClass.equals(java.lang.Integer.class)) {
            return concreteActual.equals(BigDecimal.valueOf((Integer) this.expected));
        } else if (expectedClass.equals(java.lang.Long.class)) {
            return concreteActual.equals(BigDecimal.valueOf((Long) this.expected));
        } else if (expectedClass.equals(java.lang.Float.class)) {
            return concreteActual.floatValue() == (Float) this.expected;
        } else if (expectedClass.equals(java.lang.Double.class)) {
            return concreteActual.doubleValue() == (Double) this.expected;
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
}