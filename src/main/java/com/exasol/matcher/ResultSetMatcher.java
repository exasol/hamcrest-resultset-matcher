package com.exasol.matcher;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * This matcher compares two result sets.
 */
public final class ResultSetMatcher extends TypeSafeMatcher<ResultSet> {
    private static final int EXASOL_INTERVAL_DAY_TO_SECONDS = -104;
    private static final int EXASOL_INTERVAL_YEAR_TO_MONTHS = -103;
    private final StringBuilder errorMessage = new StringBuilder();
    private final ResultSet expectedResultSet;

    /**
     * Creates a new instance of {@link ResultSetMatcher}.
     *
     * @param expectedResultSet expected Result Set
     */
    public ResultSetMatcher(final ResultSet expectedResultSet) {
        this.expectedResultSet = expectedResultSet;
    }

    /**
     * Compares against a result set.
     *
     * @param expectedResultSet expected result set
     * @return an instance of {@link ResultSetMatcher}
     */
    public static ResultSetMatcher matchesResultSet(final ResultSet expectedResultSet) {
        return new ResultSetMatcher(expectedResultSet);
    }

    @Override
    protected boolean matchesSafely(final ResultSet actualResultSet) {
        try {
            return assertEqualResultSets(actualResultSet);
        } catch (final SQLException exception) {
            throw new AssertionError(
                    "Assertion failed due to an unexpected SQL exception. Cause: " + exception.getSQLState(),
                    exception);
        }
    }

    @Override
    protected void describeMismatchSafely(final ResultSet item, final Description mismatchDescription) {
        mismatchDescription.appendText(this.errorMessage.toString());
        super.describeMismatchSafely(item, mismatchDescription);
    }

    @Override
    public void describeTo(final Description description) {
        description.appendValue(this.expectedResultSet);
    }

    private boolean assertEqualResultSets(final ResultSet actualResultSet) throws SQLException {
        final int expectedColumnCount = this.expectedResultSet.getMetaData().getColumnCount();
        final int actualColumnCount = actualResultSet.getMetaData().getColumnCount();
        if (expectedColumnCount != actualColumnCount) {
            this.errorMessage.append("Column count doesn't match. Expected column count: ").append(expectedColumnCount);
            this.errorMessage.append(", actual column count: ").append(actualColumnCount).append("\n");
            return false;
        }
        boolean expectedNext;
        int rowCounter = 0;
        do {
            expectedNext = this.expectedResultSet.next();
            rowCounter++;
            if ((expectedNext != actualResultSet.next())
                    || (this.expectedResultSet.isLast() != actualResultSet.isLast())) {
                this.errorMessage.append("Expected and actual result sets have different number of rows.\n");
                return false;
            }
            if (expectedNext && !doesRowMatch(actualResultSet, expectedColumnCount)) {
                this.errorMessage.append(", row ").append(rowCounter).append(")\n");
                return false;
            }
        } while (expectedNext);
        return true;
    }

    private boolean doesRowMatch(final ResultSet actualResultSet, final int expectedColumnCount) throws SQLException {
        for (int column = 1; column <= expectedColumnCount; ++column) {
            if (!doesFieldMatch(actualResultSet, column)) {
                this.errorMessage.append(" (column ").append(column);
                return false;
            }
        }
        return true;
    }

    private boolean doesFieldMatch(final ResultSet actualRow, final int column) throws SQLException {
        final int resultSetTypeExpected = this.expectedResultSet.getMetaData().getColumnType(column);
        final int resultSetTypeActual = actualRow.getMetaData().getColumnType(column);
        if (resultSetTypeExpected == resultSetTypeActual) {
            return doesValueMatch(actualRow, column, resultSetTypeExpected);
        } else {
            this.errorMessage.append("Data type does not match. Expected: ").append(resultSetTypeExpected);
            this.errorMessage.append(", actual: ").append(resultSetTypeActual);
            return false;
        }
    }

    private boolean doesValueMatch(final ResultSet actualRow, final int column, final int resultSetTypeExpected)
            throws SQLException {
        switch (resultSetTypeExpected) {
        case Types.BIGINT:
        case Types.SMALLINT:
        case Types.INTEGER:
            return doesIntegerMatch(actualRow, column);
        case Types.DOUBLE:
            return doesDoubleMatch(actualRow, column);
        case Types.DECIMAL:
            return doesDecimalMatch(actualRow, column);
        case Types.CHAR:
        case Types.VARCHAR:
        case EXASOL_INTERVAL_YEAR_TO_MONTHS:
        case EXASOL_INTERVAL_DAY_TO_SECONDS:
            return doesStringMatch(actualRow, column);
        case Types.BOOLEAN:
            return doesBooleanMatch(actualRow, column);
        case Types.DATE:
            return doesDateMatch(actualRow, column);
        case Types.TIMESTAMP:
            return doesTimestampMatch(actualRow, column);
        default:
            throw new AssertionError("Unknown data type: " + resultSetTypeExpected + " in column " + column
                    + ". ResultSetMatcher doesn't support comparing this data type yet.");
        }
    }

    private boolean doesTimestampMatch(final ResultSet actualRow, final int column) throws SQLException {
        final Timestamp expected = this.expectedResultSet.getTimestamp(column);
        final Timestamp actual = actualRow.getTimestamp(column);
        return doesObjectMatch("Timestamp", expected, actual);
    }

    private boolean doesDateMatch(final ResultSet actualRow, final int column) throws SQLException {
        final Date expected = this.expectedResultSet.getDate(column);
        final Date actual = actualRow.getDate(column);
        return doesObjectMatch("Date", expected, actual);
    }

    private boolean doesDecimalMatch(final ResultSet actualRow, final int column) throws SQLException {
        final BigDecimal expected = this.expectedResultSet.getBigDecimal(column);
        final BigDecimal actual = actualRow.getBigDecimal(column);
        return doesObjectMatch("BigDecimal", expected, actual);
    }

    private boolean doesBooleanMatch(final ResultSet actualRow, final int column) throws SQLException {
        final boolean expected = this.expectedResultSet.getBoolean(column);
        final boolean actual = actualRow.getBoolean(column);
        return doesPrimitiveTypeMatch("Boolean", expected, actual);
    }

    private <T> boolean doesPrimitiveTypeMatch(final String dataTypeName, final T expectedValue, final T actualValue) {
        if (expectedValue == actualValue) {
            return true;
        } else {
            writeFieldValueMismatchErrorMessage(dataTypeName, String.valueOf(expectedValue),
                    String.valueOf(actualValue));
            return false;
        }
    }

    private void writeFieldValueMismatchErrorMessage(final String valueType, final String expectedValue,
            final String actualValue) {
        this.errorMessage.append(valueType).append(" field value does not match. Expected: ").append(expectedValue);
        this.errorMessage.append(", actual: ").append(actualValue);
    }

    private boolean doesDoubleMatch(final ResultSet actualRow, final int column) throws SQLException {
        final Double expected = this.expectedResultSet.getDouble(column);
        final Double actual = actualRow.getDouble(column);
        return doesObjectMatch("Double", expected, actual);
    }

    private boolean doesIntegerMatch(final ResultSet actualRow, final int column) throws SQLException {
        final Integer expected = this.expectedResultSet.getInt(column);
        final Integer actual = actualRow.getInt(column);
        return doesObjectMatch("Integer", expected, actual);
    }

    private boolean doesStringMatch(final ResultSet actualRow, final int column) throws SQLException {
        final String expected = this.expectedResultSet.getString(column);
        final String actual = actualRow.getString(column);
        return doesObjectMatch("String", expected, actual);
    }

    private <T> boolean doesObjectMatch(final String dataTypeName, final T expectedValue, final T actualValue) {
        if ((expectedValue == null) || (actualValue == null)) {
            return doesPrimitiveTypeMatch(dataTypeName, expectedValue, actualValue);
        }
        if (expectedValue.equals(actualValue)) {
            return true;
        } else {
            writeFieldValueMismatchErrorMessage(dataTypeName, String.valueOf(expectedValue),
                    String.valueOf(actualValue));
            return false;
        }
    }
}