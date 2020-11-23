package com.exasol.matcher;

import java.math.BigDecimal;
import java.sql.*;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * This matcher compares two result sets.
 */
public final class ResultSetMatcher extends TypeSafeMatcher<ResultSet> {
    private static final int EXASOL_INTERVAL_DAY_TO_SECONDS = -104;
    private static final int EXASOL_INTERVAL_YEAR_TO_MONTHS = -103;
    private final ResultSet expectedResultSet;
    private String expectedDescription = "";
    private String actualDescription = "";
    private int rowCounter = 0;

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
        mismatchDescription.appendText(this.actualDescription);
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText(this.expectedDescription);
    }

    private boolean assertEqualResultSets(final ResultSet actualResultSet) throws SQLException {
        final int expectedColumnCount = this.expectedResultSet.getMetaData().getColumnCount();
        final int actualColumnCount = actualResultSet.getMetaData().getColumnCount();
        if (!columnCounterMatches(expectedColumnCount, actualColumnCount)) {
            return false;
        }
        boolean expectedNext;
        boolean actualNext;
        do {
            expectedNext = this.expectedResultSet.next();
            actualNext = actualResultSet.next();
            this.rowCounter++;
            if (!doBothRowsExist(actualResultSet, expectedNext, actualNext))
                return false;
            if (expectedNext && !doesRowMatch(actualResultSet, expectedColumnCount)) {
                return false;
            }
        } while (actualNext);
        return true;
    }

    private boolean columnCounterMatches(final int expectedColumnCount, final int actualColumnCount) {
        if (expectedColumnCount != actualColumnCount) {
            this.expectedDescription = "ResultSet with <" + expectedColumnCount + "> column(s)";
            this.actualDescription = "ResultSet with <" + actualColumnCount + "> column(s)";
            return false;
        } else {
            return true;
        }
    }

    private boolean doBothRowsExist(final ResultSet actualResultSet, final boolean expectedNext,
            final boolean actualNext) throws SQLException {
        if (expectedNext != actualNext) {
            final int expectedRowCounter;
            final int actualRowCounter;
            if (expectedNext) {
                expectedRowCounter = getRowCounter(this.rowCounter, this.expectedResultSet);
                actualRowCounter = this.rowCounter - 1;
            } else {
                expectedRowCounter = this.rowCounter - 1;
                actualRowCounter = getRowCounter(this.rowCounter, actualResultSet);
            }
            this.expectedDescription = "ResultSet with <" + expectedRowCounter + "> row(s)";
            this.actualDescription = "ResultSet with <" + actualRowCounter + "> row(s)";
            return false;
        } else {
            return true;
        }
    }

    private int getRowCounter(final int rowCounter, final ResultSet resultSet) throws SQLException {
        int counter = rowCounter;
        while (resultSet.next()) {
            counter++;
        }
        return counter;
    }

    private boolean doesRowMatch(final ResultSet actualResultSet, final int expectedColumnCount) throws SQLException {
        for (int column = 1; column <= expectedColumnCount; ++column) {
            if (!doesFieldMatch(actualResultSet, column)) {
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
            this.expectedDescription = "Column <" + column + "> with JDBC Data Type " + resultSetTypeExpected;
            this.actualDescription = "Column <" + column + "> with JDBC Data Type " + resultSetTypeActual;
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
        return doesObjectMatch("Timestamp", expected, actual, column);
    }

    private boolean doesDateMatch(final ResultSet actualRow, final int column) throws SQLException {
        final Date expected = this.expectedResultSet.getDate(column);
        final Date actual = actualRow.getDate(column);
        return doesObjectMatch("Date", expected, actual, column);
    }

    private boolean doesDecimalMatch(final ResultSet actualRow, final int column) throws SQLException {
        final BigDecimal expected = this.expectedResultSet.getBigDecimal(column);
        final BigDecimal actual = actualRow.getBigDecimal(column);
        return doesObjectMatch("BigDecimal", expected, actual, column);
    }

    private boolean doesBooleanMatch(final ResultSet actualRow, final int column) throws SQLException {
        final boolean expected = this.expectedResultSet.getBoolean(column);
        final boolean actual = actualRow.getBoolean(column);
        return doesPrimitiveTypeMatch("Boolean", expected, actual, column);
    }

    private <T> boolean doesPrimitiveTypeMatch(final String dataTypeName, final T expectedValue, final T actualValue,
            final int column) {
        if (expectedValue == actualValue) {
            return true;
        } else {
            writeFieldValueMismatchErrorMessage(dataTypeName, String.valueOf(expectedValue),
                    String.valueOf(actualValue), column);
            return false;
        }
    }

    private void writeFieldValueMismatchErrorMessage(final String valueType, final String expectedValue,
            final String actualValue, final int column) {
        this.expectedDescription = valueType + " field value <" + expectedValue + ">" + " (column " + column + ", row "
                + this.rowCounter + ")";
        this.actualDescription = valueType + " field value <" + actualValue + ">" + " (column " + column + ", row "
                + this.rowCounter + ")";
    }

    private boolean doesDoubleMatch(final ResultSet actualRow, final int column) throws SQLException {
        final Double expected = this.expectedResultSet.getDouble(column);
        final Double actual = actualRow.getDouble(column);
        return doesObjectMatch("Double", expected, actual, column);
    }

    private boolean doesIntegerMatch(final ResultSet actualRow, final int column) throws SQLException {
        final Integer expected = this.expectedResultSet.getInt(column);
        final Integer actual = actualRow.getInt(column);
        return doesObjectMatch("Integer", expected, actual, column);
    }

    private boolean doesStringMatch(final ResultSet actualRow, final int column) throws SQLException {
        final String expected = this.expectedResultSet.getString(column);
        final String actual = actualRow.getString(column);
        return doesObjectMatch("String", expected, actual, column);
    }

    private <T> boolean doesObjectMatch(final String dataTypeName, final T expectedValue, final T actualValue,
            final int column) {
        if ((expectedValue == null) || (actualValue == null)) {
            return doesPrimitiveTypeMatch(dataTypeName, expectedValue, actualValue, column);
        }
        if (expectedValue.equals(actualValue)) {
            return expectedValue.equals(actualValue);
        } else {
            writeFieldValueMismatchErrorMessage(dataTypeName, String.valueOf(expectedValue),
                    String.valueOf(actualValue), column);
            return false;
        }
    }
}