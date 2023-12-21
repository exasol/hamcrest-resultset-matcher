package com.exasol.matcher;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

import org.hamcrest.*;

/**
 * Hamcrest matcher that compares JDBC result sets against Java object structures.
 * <p>
 * The matcher supports strict type matching and a fuzzy mode. In fuzzy mode it value matches are accepted if a the
 * matcher knows how to convert between the expected type and the actual and the converted value matches.
 * </p>
 */
public class ResultSetStructureMatcher extends TypeSafeMatcher<ResultSet> {
    private final List<List<Matcher<?>>> cellMatcherTable;
    private final List<Column> expectedColumns;
    private static final Logger LOGGER = Logger.getLogger(ResultSetStructureMatcher.class.getName());
    private final Calendar calendar;
    private boolean isCalendarWarningDisplayed;
    private int actualRowCount;
    private boolean contentDeviates;
    private int deviationStartColumn;
    private int deviationStartRow;
    private final List<Column> actualColumns = new ArrayList<>();
    private final TypeMatchMode typeMatchMode;
    private final boolean requireSameOrder;
    private final BigDecimal tolerance;
    private final Description cellDescription = new StringDescription();
    private final Description cellMismatchDescription = new StringDescription();
    private boolean ambiguousRowMatch;

    private ResultSetStructureMatcher(final Builder builder) {
        this.expectedColumns = builder.expectedColumns;
        this.typeMatchMode = builder.typeMatchMode;
        this.requireSameOrder = builder.requireSameOrder;
        this.tolerance = builder.tolerance;
        this.calendar = builder.calendar;
        this.contentDeviates = false;
        this.cellMatcherTable = wrapExpectedValuesInMatchers(builder);
        this.isCalendarWarningDisplayed = false;
    }

    private List<List<Matcher<?>>> wrapExpectedValuesInMatchers(final Builder builder) {
        final List<List<Matcher<?>>> tableOfMatchers = new ArrayList<>(builder.rows);
        for (final List<Object> expectedRow : builder.expectedTable) {
            final List<Matcher<?>> cellMatchers = wrapExpectedRowInMatchers(expectedRow);
            tableOfMatchers.add(cellMatchers);
        }
        return tableOfMatchers;
    }

    private static Matcher<?> castToMatcher(final Object expectedCellValue) {
        return (Matcher<?>) expectedCellValue;
    }

    private List<Matcher<?>> wrapExpectedRowInMatchers(final List<Object> expectedRow) {
        final List<Matcher<?>> rowOfMatchers = new ArrayList<>(expectedRow.size());
        for (final Object expectedCellValue : expectedRow) {
            if (expectedCellValue instanceof Matcher<?>) {
                rowOfMatchers.add(castToMatcher(expectedCellValue));
            } else {
                rowOfMatchers
                        .add(CellMatcherFactory.cellMatcher(expectedCellValue, this.typeMatchMode, this.tolerance));
            }
        }
        return rowOfMatchers;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("ResultSet with ") //
                .appendValue(this.cellMatcherTable.size()) //
                .appendText(" rows and ") //
                .appendValue(getExpectedColumnCount()) //
                .appendText(" columns");
        if (isAnyColumnDetailSpecified()) {
            description.appendList(" (", ", ", ")", this.expectedColumns);
        }
    }

    private boolean isAnyColumnDetailSpecified() {
        for (final Column column : this.expectedColumns) {
            if (column.isSpecified()) {
                return true;
            }
        }
        return false;
    }

    // Note that we can't iterate over the rows again here since not all JDBC drivers support rewinding a result set.
    // That means we need to already collect all information we want to display here during the match.
    @Override
    protected void describeMismatchSafely(final ResultSet item, final Description mismatchDescription) {
        mismatchDescription.appendText("ResultSet with ") //
                .appendValue(this.actualRowCount) //
                .appendText(" rows and ") //
                .appendValue(this.actualColumns.size()) //
                .appendText(" columns");
        if (isAnyColumnDetailSpecified()) {
            mismatchDescription //
                    .appendList(" (", ", ", ")", this.actualColumns);
        }
        if (this.ambiguousRowMatch) {
            mismatchDescription.appendText(" where at least one expected row matched multiple result rows. "
                    + "Please narrow down the matching criteria to avoid ambiguity.");
        } else if (this.contentDeviates) {
            if (this.requireSameOrder) {
                mismatchDescription.appendText(" where content deviates starting row ") //
                        .appendValue(this.deviationStartRow) //
                        .appendText(", column ") //
                        .appendValue(this.deviationStartColumn) //
                        .appendText(": expected was ") //
                        .appendText(this.cellDescription.toString())//
                        .appendText(" but ")//
                        .appendText(this.cellMismatchDescription.toString());
            } else {
                mismatchDescription.appendText(" where row ") //
                        .appendValue(this.deviationStartRow) //
                        .appendText(" was the first that did not match any expected row");
            }
        }
    }

    @Override
    protected boolean matchesSafely(final ResultSet resultSet) {
        final boolean columnsOk = matchColumns(resultSet);
        final boolean rowsOk = this.requireSameOrder ? matchRowsInOrder(resultSet) : matchRowsInAnyOrder(resultSet);
        return columnsOk && rowsOk;
    }

    private boolean matchRowsInOrder(final ResultSet resultSet) {
        boolean ok = true;
        try {
            int rowIndex = 0;
            int matcherRowIndex = 0;
            for (final List<Matcher<?>> cellMatcherRow : this.cellMatcherTable) {
                ++matcherRowIndex;
                if (resultSet.next()) {
                    ++rowIndex;
                    ok = ok && matchValuesInRow(resultSet, rowIndex, matcherRowIndex, cellMatcherRow, true);
                } else {
                    ok = false;
                }
            }
            while (resultSet.next()) {
                ok = false;
                ++rowIndex;
            }
            this.actualRowCount = rowIndex;
            return ok;
        } catch (final SQLException exception) {
            throw new AssertionError("Unable to check result set: " + exception.getMessage());
        }
    }

    private boolean matchRowsInAnyOrder(final ResultSet resultSet) {
        boolean ok = true;
        try {
            final int numberOfRowMatchers = this.cellMatcherTable.size();
            final int[] matchesForRowMatcher = new int[numberOfRowMatchers];
            int rowIndex = 0;
            int matcherRowIndex = 0;
            while (resultSet.next()) {
                ++rowIndex;
                boolean anyMatchForThisResultRow = false;
                int matcherIndex = 0;
                for (final List<Matcher<?>> cellMatcherRow : this.cellMatcherTable) {
                    ++matcherRowIndex;
                    if (matchValuesInRow(resultSet, rowIndex, matcherRowIndex, cellMatcherRow, false)) {
                        ++matchesForRowMatcher[matcherIndex];
                        anyMatchForThisResultRow = true;
                    }
                    ++matcherIndex;
                }
                recordRowMatchResult(rowIndex, anyMatchForThisResultRow);
                ok = ok && anyMatchForThisResultRow;
            }
            this.actualRowCount = rowIndex;
            if (validateAllMatchersMatchedExactlyOnce(numberOfRowMatchers, matchesForRowMatcher)) {
                return ok;
            } else {
                return false;
            }
        } catch (final SQLException exception) {
            throw new AssertionError("Unable to check result set: " + exception.getMessage());
        }
    }

    private void recordRowMatchResult(final int rowIndex, final boolean anyMatchForThisResultRow) {
        if (!anyMatchForThisResultRow && !this.contentDeviates) {
            this.contentDeviates = true;
            this.deviationStartRow = rowIndex;
        }
    }

    private boolean validateAllMatchersMatchedExactlyOnce(final int numberOfRowMatchers,
            final int[] matchesForRowMatcher) {
        for (int matcherIndex = 0; matcherIndex < numberOfRowMatchers; ++matcherIndex) {
            if (matchesForRowMatcher[matcherIndex] == 0) {
                return false;
            } else if (matchesForRowMatcher[matcherIndex] > 1) {
                this.ambiguousRowMatch = true;
                return false;
            }
        }
        return true;
    }

    private boolean matchColumns(final ResultSet resultSet) {
        try {
            final ResultSetMetaData metadata = resultSet.getMetaData();
            final int actualColumnCount = metadata.getColumnCount();
            boolean ok = (actualColumnCount == getExpectedColumnCount());
            for (int columnIndex = 1; columnIndex <= getExpectedColumnCount(); ++columnIndex) {
                final String actualColumnTypeName = metadata.getColumnTypeName(columnIndex);
                final Column expectedColumn = this.expectedColumns.get(columnIndex - 1);
                if (expectedColumn.hasType()) {
                    ok = ok && actualColumnTypeName.equalsIgnoreCase(expectedColumn.getTypeName());
                }
                this.actualColumns.add(Column.column(actualColumnTypeName));
            }
            for (int columnIndex = getExpectedColumnCount() + 1; columnIndex <= actualColumnCount; columnIndex++) {
                this.actualColumns.add(Column.column(metadata.getColumnTypeName(columnIndex)));
            }
            return ok;
        } catch (final SQLException exception) {
            return false;
        }
    }

    private int getExpectedColumnCount() {
        return this.expectedColumns.size();
    }

    /**
     * Match the values in a result row.
     * <p>
     * You can optionally record the first mismatch. This is useful in case you have exactly one attempt to match a row.
     * If you try against multiple matchers (e.g. when matching rows in any order), the first mismatch might be OK, so
     * recording it at this early stage is not useful.
     * </p>
     *
     * @param resultSet            result set from which to read the cell values
     * @param rowIndex             index of the row in the result set
     * @param matcherRowIndex      index of the matcher definition
     * @param cellMatcherRow       list of matchers that are tested against the row's cells
     * @param recordFirstDeviation record the first mismatch when set to {@code true}
     * @return {@code true} if the given matchers match all cells in this row
     */
    private boolean matchValuesInRow(final ResultSet resultSet, final int rowIndex, final int matcherRowIndex,
            final List<Matcher<?>> cellMatcherRow, final boolean recordFirstDeviation) {
        int columnIndex = 0;
        try {
            for (final Matcher<?> cellMatcher : cellMatcherRow) {
                ++columnIndex;
                final Object value = readCellValue(resultSet, columnIndex);
                if (!cellMatcher.matches(value)) {
                    if (recordFirstDeviation) {
                        recordFirstDeviation(value, cellMatcher, rowIndex, columnIndex);
                    }
                    return false;
                }
            }
        } catch (final SQLException exception) {
            throw new AssertionError("Row expectation definition " + matcherRowIndex
                    + " tries to validate the value of row " + rowIndex + ", column " + columnIndex
                    + " but that value can't be read from the result set. "
                    + "This usually means the column does not exist. \nCaused by SQL error: " + exception.getMessage());
        }
        return true;
    }

    private Object readCellValue(final ResultSet resultSet, final int columnIndex) throws SQLException {
        final Object value = resultSet.getObject(columnIndex);
        if (value instanceof java.sql.Timestamp) {
            displayCalendarWarningIfRequired();
            if (this.calendar != null) {
                return resultSet.getTimestamp(columnIndex, this.calendar);
            } else {
                return value;
            }
        } else if (value instanceof java.sql.Date) {
            displayCalendarWarningIfRequired();
            if (this.calendar != null) {
                return resultSet.getDate(columnIndex, this.calendar);
            } else {
                return value;
            }
        } else {
            return value;
        }
    }

    private void displayCalendarWarningIfRequired() {
        if (this.calendar == null && !this.isCalendarWarningDisplayed) {
            displayCalendarWarning();
        }
    }

    private void displayCalendarWarning() {
        LOGGER.warning(
                () -> "Reading a timestamp or date value without configured calendar. That's dangerous since the JDBC driver is using the time-zone of the test system in that case. "
                        + "You can fix this by providing a calendar using 'withCalendar(Calendar)'. For example 'Calendar.getInstance(TimeZone.getTimeZone(\"UTC\"))'.");
        this.isCalendarWarningDisplayed = true;
    }

    private void recordFirstDeviation(final Object value, final Matcher<?> cellMatcher, final int rowIndex,
            final int columnIndex) {
        this.contentDeviates = true;
        this.deviationStartRow = rowIndex;
        this.deviationStartColumn = columnIndex;
        cellMatcher.describeTo(this.cellDescription);
        cellMatcher.describeMismatch(value, this.cellMismatchDescription);
    }

    /**
     * Builder for a {@link ResultSetMatcher} that ignores the column metadata.
     *
     * @return Builder instance
     */
    public static Builder table() {
        return new Builder();
    }

    /**
     * Builder for a {@link ResultSetMatcher} that ignores the column metadata.
     *
     * @param types description of the expected result set columns
     *
     * @return Builder instance
     */
    public static Builder table(final String... types) {
        final Builder builder = new Builder();
        for (final String type : types) {
            builder.addExpectedColumn(Column.column(type));
        }
        return builder;
    }

    /**
     * Builder for {@link ResultSetStructureMatcher} objects.
     */
    public static final class Builder {
        private final List<List<Object>> expectedTable = new ArrayList<>();
        private int rows = 0;
        private List<Column> expectedColumns = new ArrayList<>();
        private TypeMatchMode typeMatchMode;
        private BigDecimal tolerance = BigDecimal.ZERO;
        private Calendar calendar;
        private boolean requireSameOrder = true;

        /**
         * Create a new instance of a {@link ResultSetStructureMatcher.Builder}.
         */
        public Builder() {
            // intentionally empty
        }

        /**
         * Add a column to the structure to be matched.
         * 
         * @param expectedColumn the expected column
         */
        public void addExpectedColumn(final Column expectedColumn) {
            this.expectedColumns.add(expectedColumn);
        }

        /**
         * Add a row to the structure to be matched.
         *
         * @param cellValues values of the cells in the row
         * @return {@code this} for fluent programming
         */
        public Builder row(final Object... cellValues) {
            ++this.rows;
            final int length = cellValues.length;
            if (this.expectedColumns.isEmpty()) {
                setColumnCountExpectation(length);
            } else {
                validateColumnCount(length);
            }
            this.expectedTable.add(Arrays.asList(cellValues));
            return this;
        }

        /**
         * Adds a tolerance value for fuzzy matching floating point values.
         *
         * @param tolerance a tolerance value for matching floating point values
         * @return {@code this} for fluent programming
         */
        public Builder withDefaultNumberTolerance(final BigDecimal tolerance) {
            this.tolerance = tolerance;
            return this;
        }

        private void setColumnCountExpectation(final int length) {
            this.expectedColumns = new ArrayList<>(length);
            for (int i = 0; i < length; ++i) {
                this.expectedColumns.add(Column.any());
            }
        }

        private void validateColumnCount(final int length) throws AssertionError {
            if (length != this.expectedColumns.size()) {
                throw new AssertionError("Error constructing expected row " + this.rows + ". Expected "
                        + this.expectedColumns.size() + " columns, but got " + length + ".");
            }
        }

        /**
         * Set a calendar to use for decoding the value of {@code TIMESTAMP}, {@code TIMESTAMP WITH LOCAL TIME ZONE},
         * and {@code DATE} columns.
         * 
         * @param calendar calendar to use
         * @return self for fluent programming
         */
        public Builder withCalendar(final Calendar calendar) {
            this.calendar = calendar;
            return this;
        }

        /**
         * This method configure the matcher to use a UTC calendar for decoding the value of {@code TIMESTAMP},
         * {@code TIMESTAMP WITH LOCAL TIME ZONE}, and {@code DATE} columns.
         *
         * @return self for fluent programming
         */
        public Builder withUtcCalendar() {
            return withCalendar(Calendar.getInstance(TimeZone.getTimeZone("UTC")));
        }

        /**
         * Create a new matcher that matches cell types strictly.
         *
         * @return matcher
         */
        public Matcher<ResultSet> matches() {
            return matches(TypeMatchMode.STRICT);
        }

        /**
         * Create a new matcher that matches cell types depending on type match mode.
         * 
         * @param typeMatchMode mode for type matching
         * @return matcher
         */
        public Matcher<ResultSet> matches(final TypeMatchMode typeMatchMode) {
            this.typeMatchMode = typeMatchMode;
            return new ResultSetStructureMatcher(this);
        }

        /**
         * Create a matcher that ignores the order of the result rows with strict cell type checking.
         * 
         * @return matcher the new matcher
         */
        public Matcher<ResultSet> matchesInAnyOrder() {
            return matchesInAnyOrder(TypeMatchMode.STRICT);
        }

        /**
         * Create a matcher that ignores the order of the result rows with a given type match mode.
         * 
         * @param typeMatchMode mode for type matching
         * @return matcher the new matcher
         */
        private Matcher<ResultSet> matchesInAnyOrder(final TypeMatchMode typeMatchMode) {
            this.requireSameOrder = false;
            this.typeMatchMode = typeMatchMode;
            return new ResultSetStructureMatcher(this);
        }

        /**
         * Create a new matcher that matches cell types fuzzily.
         * 
         * @deprecated use {@link #matches(TypeMatchMode)} with {@link TypeMatchMode#NO_JAVA_TYPE_CHECK} instead.
         * @return matcher
         */
        @Deprecated(since = "1.3.0")
        public Matcher<ResultSet> matchesFuzzily() {
            this.typeMatchMode = TypeMatchMode.NO_JAVA_TYPE_CHECK;
            return new ResultSetStructureMatcher(this);
        }
    }
}
