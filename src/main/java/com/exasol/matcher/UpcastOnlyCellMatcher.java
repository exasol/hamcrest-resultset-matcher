package com.exasol.matcher;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * This matcher checks, that the actual cell has a type that can be safely up-cast to the expected type.
 * <p>
 * If the actual or expected types are not numeric, this matcher always matches.
 * </p>
 * <p>
 * This matcher does <b>not</b> check the value of the cell. Use a second matcher for that (for example the
 * {@link FuzzyCellMatcher}).
 * </p>
 */
public class UpcastOnlyCellMatcher<T> extends TypeSafeMatcher<T> {
    private static final List<Class<?>> DECIMAL_TYPES = List.of(Byte.class, Short.class, Integer.class, Long.class,
            BigInteger.class, BigDecimal.class);
    private static final List<Class<?>> FLOATING_POINT_TYPES = List.of(Float.class, Double.class);

    private final T expected;
    private String explanation = "";

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
    protected boolean matchesSafely(final T actual) {
        this.explanation = "";
        if (actual instanceof Number && this.expected instanceof Number) {
            return checkNumbersAreOnlyUpcasted(actual, this.expected);
        } else {
            return actual.getClass().equals(this.expected.getClass());
        }
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("type that can safely be cast to ").appendText(this.expected.getClass().getName());
    }

    @Override
    protected void describeMismatchSafely(final T item, final Description mismatchDescription) {
        mismatchDescription.appendText(this.explanation);
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
            this.explanation = "Can not cast from actual floating point to expected non-floating point type.";
            return false;
        }
    }

    private boolean checkDecimalToFloatUpcast(final Object actual) {
        final int actualIndex = DECIMAL_TYPES.indexOf(actual.getClass());
        if (actualIndex <= DECIMAL_TYPES.indexOf(Short.class)) {
            return true; // we can safely cast a SHORT to a float or double
        } else if (actualIndex <= DECIMAL_TYPES.indexOf(Integer.class)
                && this.expected.getClass().equals(Double.class)) {
            return true; // we can safely cast an Integer to a double
        } else {
            setFloatingPointExplanation();
            return false;
        }
    }

    private boolean checkFloatingPointUpcast(final Object actual, final Object expected) {
        if (FLOATING_POINT_TYPES.indexOf(actual.getClass()) <= FLOATING_POINT_TYPES.indexOf(expected.getClass())) {
            return true;
        } else {
            setActualTypeBiggerThatExpectedExplanation();
            return false;
        }
    }

    private boolean checkDecimalUpcast(final Object actual) {
        if (DECIMAL_TYPES.indexOf(actual.getClass()) <= DECIMAL_TYPES.indexOf(this.expected.getClass())) {
            return true;
        } else {
            setActualTypeBiggerThatExpectedExplanation();
            return false;
        }
    }

    private void setActualTypeBiggerThatExpectedExplanation() {
        this.explanation = "The actual type is bigger than the expected. You can disable this check by using the NO_JAVA_TYPE_CHECK fuzzy-mode.";
    }

    private void setFloatingPointExplanation() {
        this.explanation = "Illegal upcast. Upcasts are only allowed from non floating types <= short to float and from types <= integer to double.";
    }
}
