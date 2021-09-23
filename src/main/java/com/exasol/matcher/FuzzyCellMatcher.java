package com.exasol.matcher;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

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
    private final BigDecimal tolerance;
    private BigDecimal lastDifference;

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
     * Create a new instance of a {@link FuzzyCellMatcher} with zero tolerance.
     *
     * @param expected the expected cell content.
     */
    public FuzzyCellMatcher(final T expected) {
        this(expected, BigDecimal.ZERO);
    }

    /**
     * Create a new instance of a {@link FuzzyCellMatcher} with configurable tolerance for number matching.
     *
     * @param expected  the expected cell content.
     * @param tolerance tolerance for number matching.
     */
    public FuzzyCellMatcher(final T expected, final BigDecimal tolerance) {
        this.expected = expected;
        this.tolerance = tolerance;
    }

    /**
     * Creates a {@link FuzzyCellMatcher} that matches cell values in a more relaxed fashion with a tolerance for number
     * matching.
     *
     * @param <T>       the expected type
     * @param expected  expected value
     * @param tolerance tolerance for number matching
     * @return new matcher instance
     */
    public static <T> FuzzyCellMatcher<T> fuzzilyEqualTo(final T expected, final BigDecimal tolerance) {
        return new FuzzyCellMatcher<>(expected, tolerance);
    }

    @Override
    public boolean matches(final Object actual) {
        this.lastDifference = null;
        if ((actual instanceof Number && this.expected instanceof Number)
                || (actual instanceof Number && this.expected instanceof String)
                || (actual instanceof String && this.expected instanceof Number)) {
            try {
                return compareAsNumbers(actual);
            } catch (final NumberFormatException exception) {
                return defaultCompare(actual);
            }
        } else if (actual instanceof java.sql.Date && this.expected instanceof java.util.Date) {
            final Instant actualDate = Instant.ofEpochMilli(((java.sql.Date) actual).getTime())
                    .truncatedTo(ChronoUnit.DAYS);
            final Instant expectedDate = Instant.ofEpochMilli(((java.util.Date) this.expected).getTime())
                    .truncatedTo(ChronoUnit.DAYS);
            return actualDate.equals(expectedDate);
        } else {
            return defaultCompare(actual);
        }
    }

    private boolean defaultCompare(final Object actual) {
        return actual.equals(this.expected);
    }

    private boolean compareAsNumbers(final Object actual) {
        final BigDecimal actualBigDecimal = new BigDecimal(actual.toString());
        final BigDecimal expectedBigDecimal = new BigDecimal(this.expected.toString());
        if (actualBigDecimal.compareTo(expectedBigDecimal) == 0) {
            return true;
        } else {
            return checkNumbersWithTolerance(actualBigDecimal, expectedBigDecimal);
        }
    }

    private boolean checkNumbersWithTolerance(final BigDecimal actualBigDecimal, final BigDecimal expectedBigDecimal) {
        this.lastDifference = actualBigDecimal.subtract(expectedBigDecimal).abs();
        return this.lastDifference.compareTo(this.tolerance) < 1;
    }

    @Override
    public void describeTo(final Description description) {
        if (isMatchingWithToleranceEnabled()) {
            description.appendText("a value close to ").appendValue(this.expected).appendText(" (tolerance: +/- ")
                    .appendValue(this.tolerance).appendText(")");
        } else {
            description.appendText("a value equal to ");
            description.appendValue(improveToStringOfTimestampAndDate(this.expected));
        }
    }

    /**
     * Improve the string representation of {@link Timestamp} and {@link Date}.
     * <p>
     * The {@code toString} method of {@link Timestamp} and {@link Date} are dependent of the system timezone while the
     * values are not. That can be confusing for testing. For that reason, we decided to print the values in UTC here.
     * Testers can notice that by the {@code Z} at the end of the timestamp.
     * </p>
     * 
     * @param actual object to improve.
     * @return object or string
     */
    private Object improveToStringOfTimestampAndDate(final Object actual) {
        if (actual instanceof Timestamp) {
            return ((Timestamp) actual).toInstant().toString();
        } else if (actual instanceof Date) {
            final OffsetDateTime dateTime = Instant.ofEpochMilli(((Date) actual).getTime()).truncatedTo(ChronoUnit.DAYS)
                    .atOffset(ZoneOffset.UTC);
            return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE);
        } else {
            return actual;
        }
    }

    private boolean isMatchingWithToleranceEnabled() {
        return this.tolerance.compareTo(BigDecimal.ZERO) != 0;
    }

    @Override
    public void describeMismatch(final Object item, final Description description) {
        description.appendText(" was ") //
                .appendValue(improveToStringOfTimestampAndDate(item));
        if (isMatchingWithToleranceEnabled() && this.lastDifference != null) {
            description.appendText(" difference was ").appendValue(this.lastDifference);
        }
    }
}
