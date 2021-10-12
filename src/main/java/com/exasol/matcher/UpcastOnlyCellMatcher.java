package com.exasol.matcher;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * This matcher checks, that the actual cell has a type that can be safely up-cast to the expected type.
 * <p>
 * If the actual or expected types are not numeric, this matcher always matches.
 * </p>
 * <p>
 * This matcher does <b>not</b> check the value of the cell. Use a second matcher for that (for example the
 * {@link FuzzyCellMatcher}).
 * </p>
 *
 * @param <T> matcher type
 */
public class UpcastOnlyCellMatcher<T> extends BaseMatcher<T> {
    private static final List<Class<?>> DECIMAL_TYPES = List.of(Byte.class, Short.class, Integer.class, Long.class,
            BigInteger.class, BigDecimal.class);
    private static final List<Class<?>> FLOATING_POINT_TYPES = List.of(Float.class, Double.class);

    private final T expected;
    private Mismatch lastMismatch;

    @Override
    public boolean matches(final Object actual) {
        this.lastMismatch = null;
        if (actual == null) {
            return this.expected == null;
        } else if ((actual instanceof Number) && (this.expected instanceof Number)) {
            return checkNumbersAreOnlyUpcasted(actual, this.expected);
        } else {
            return actual.getClass().equals(this.expected.getClass());
        }
    }

    private UpcastOnlyCellMatcher(final T expected) {
        this.expected = expected;
    }

    /**
     * Create a new instance of an {@link UpcastOnlyCellMatcher}.
     *
     * @param expected expected value
     * @param <T>      type of the matched value
     * @return built {@link UpcastOnlyCellMatcher}
     */
    public static <T> UpcastOnlyCellMatcher<T> isOnlyUpcastTo(final T expected) {
        return new UpcastOnlyCellMatcher<>(expected);
    }

    @Override
    public void describeMismatch(final Object object, final Description mismatchDescription) {
        switch (this.lastMismatch) {
        case FLOAT_DECIMAL_MISMATCH:
            mismatchDescription
                    .appendText("Can not cast from actual floating point to expected non-floating point type.");
            break;
        case ACTUAL_BIGGER_THAN_EXPECTED:
            mismatchDescription.appendText("The actual type is bigger than the expected."
                    + " You can disable this check by using the NO_JAVA_TYPE_CHECK fuzzy-mode.");
            break;
        case DECIMAL_BIGGER_THAN_FLOATING_POINT:
            mismatchDescription
                    .appendText("Illegal upcast. Upcasts are only allowed from non floating types <= short to float"
                            + " and from types <= integer to double.");
            break;
        default:
            break;
        }
    }

    @Override
    public void describeTo(final Description description) {
        if (this.expected == null) {
            description.appendText("null value (type casting ignored)");
        } else {
            description.appendText("type that can safely be cast to ").appendText(this.expected.getClass().getName());
        }
    }

    private boolean checkNumbersAreOnlyUpcasted(final Object actual, final Object expected) {
        if (DECIMAL_TYPES.contains(actual.getClass()) && DECIMAL_TYPES.contains(expected.getClass())) {
            return checkDecimalUpcast(actual);
        } else if (FLOATING_POINT_TYPES.contains(actual.getClass())
                && FLOATING_POINT_TYPES.contains(expected.getClass())) {
            return checkFloatingPointUpcast(actual, expected);
        } else if (DECIMAL_TYPES.contains(actual.getClass()) && FLOATING_POINT_TYPES.contains(expected.getClass())) {
            return checkDecimalToFloatUpcast(actual);
        } else {
            this.lastMismatch = Mismatch.FLOAT_DECIMAL_MISMATCH;
            return false;
        }
    }

    private boolean checkDecimalToFloatUpcast(final Object actual) {
        final int actualIndex = DECIMAL_TYPES.indexOf(actual.getClass());
        if (actualIndex <= DECIMAL_TYPES.indexOf(Short.class)) {
            return true; // we can safely cast a SHORT to a float or double
        } else if ((actualIndex <= DECIMAL_TYPES.indexOf(Integer.class))
                && this.expected.getClass().equals(Double.class)) {
            return true; // we can safely cast an Integer to a double
        } else {
            this.lastMismatch = Mismatch.DECIMAL_BIGGER_THAN_FLOATING_POINT;
            return false;
        }
    }

    private boolean checkFloatingPointUpcast(final Object actual, final Object expected) {
        if (FLOATING_POINT_TYPES.indexOf(actual.getClass()) <= FLOATING_POINT_TYPES.indexOf(expected.getClass())) {
            return true;
        } else {
            this.lastMismatch = Mismatch.ACTUAL_BIGGER_THAN_EXPECTED;
            return false;
        }
    }

    private boolean checkDecimalUpcast(final Object actual) {
        if (DECIMAL_TYPES.indexOf(actual.getClass()) <= DECIMAL_TYPES.indexOf(this.expected.getClass())) {
            return true;
        } else {
            this.lastMismatch = Mismatch.ACTUAL_BIGGER_THAN_EXPECTED;
            return false;
        }
    }

    private enum Mismatch {
        FLOAT_DECIMAL_MISMATCH, ACTUAL_BIGGER_THAN_EXPECTED, DECIMAL_BIGGER_THAN_FLOATING_POINT
    }
}