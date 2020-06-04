package com.exasol.matcher;

import java.math.BigDecimal;

/**
 * Fuzzy matcher that matches cell contents with expected contents.
 * <p>
 * If the matcher knows a conversion between expected and actual type, it applies that conversion and tries to match the
 * values after that conversion.
 * </p>
 */
public class FuzzyCellMatcher implements CellMatcher {
    @Override
    public boolean match(final Object actual, final Object expected) {
        final Class<?> actualClass = actual.getClass();
        final Class<?> expectedClass = expected.getClass();
        if (actualClass.equals(expectedClass)) {
            return actual.equals(expected);
        } else if (actualClass.equals(java.math.BigDecimal.class)) {
            return matchBigDecimal(actual, expected, expectedClass);
        } else {
            return actual.equals(expected);
        }
    }

    private boolean matchBigDecimal(final Object actual, final Object expected, final Class<?> expectedClass) {
        final BigDecimal concreteActual = (BigDecimal) actual;
        if (expectedClass.equals(java.lang.Byte.class)) {
            return concreteActual.equals(BigDecimal.valueOf((Byte) expected));
        } else if (expectedClass.equals(java.lang.Short.class)) {
            return concreteActual.equals(BigDecimal.valueOf((Short) expected));
        } else if (expectedClass.equals(java.lang.Integer.class)) {
            return concreteActual.equals(BigDecimal.valueOf((Integer) expected));
        } else if (expectedClass.equals(java.lang.Long.class)) {
            return concreteActual.equals(BigDecimal.valueOf((Long) expected));
        } else if (expectedClass.equals(java.lang.Float.class)) {
            return concreteActual.floatValue() == (Float) expected;
        } else if (expectedClass.equals(java.lang.Double.class)) {
            return concreteActual.doubleValue() == (Double) expected;
        } else {
            return actual.equals(expected);
        }
    }
}