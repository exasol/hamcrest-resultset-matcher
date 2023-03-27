package com.exasol.matcher;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

import org.hamcrest.*;

import com.exasol.errorreporting.ExaError;

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
    private final BigDecimal tolerance;
    private final Description cellDescription = new StringDescription();
    private final Description cellMismatchDescription = new StringDescription();

    private ResultSetStructureMatcher(final Builder builder) {
        this.expectedColumns = builder.expectedColumns;
        this.typeMatchMode = builder.typeMatchMode;
        this.tolerance = builder.tolerance;
        this.calendar = builder.calendar;
        this.contentDeviates = false;
        this.cellMatcherTable = wrapExpectedValuesInMatchers(builder);
        this.isCalendarWarningDisplayed = false;
    }

    private List<List<Matcher<?>>> wrapExpectedValuesInMatchers(final Builder builder) {
        final List<List<Matcher<?>>> tableOfMatchers = new ArrayList<>(builder.rows);
        for (final List<Object> expectedRow : builder.expectedTable) {
            final List<Matcher<?>> cellMatchers = wrapExpecteRowInMatchers(expectedRow);
            tableOfMatchers.add(cellMatchers);
        }
        return tableOfMatchers;
    }

    private static Matcher<?> castToMatcher(final Object expectedCellValue) {
        return (Matcher<?>) expectedCellValue;
    }

    private List<Matcher<?>> wrapExpecteRowInMatchers(final List<Object> expectedRow) {
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
        if (this.contentDeviates) {
            mismatchDescription.appendText(" where content deviates starting row ") //
                    .appendValue(this.deviationStartRow) //
                    .appendText(", column ") //
                    .appendValue(this.deviationStartColumn) //
                    .appendText(": expected was ") //
                    .appendText(this.cellDescription.toString())//
                    .appendText(" but ")//
                    .appendText(this.cellMismatchDescription.toString());
        }
    }

    @Override
    protected boolean matchesSafely(final ResultSet resultSet) {
        boolean ok = matchColumns(resultSet);
        try {
            int rowIndex = 0;
            for (final List<Matcher<?>> cellMatcherRow : this.cellMatcherTable) {
                if (resultSet.next()) {
                    ++rowIndex;
                    ok = ok && matchValuesInRowMatch(resultSet, rowIndex, cellMatcherRow);
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

    private boolean matchValuesInRowMatch(final ResultSet resultSet, final int rowIndex,
            final List<Matcher<?>> cellMatcherRow) {
        int columnIndex = 0;
        try {
            for (final Matcher<?> cellMatcher : cellMatcherRow) {
                ++columnIndex;
                final Object value = readCellValue(resultSet, columnIndex);
                if (!matchCell(value, cellMatcher, rowIndex, columnIndex)) {
                    return false;
                }
            }
        } catch (final SQLException exception) {
            throw new AssertionError("Unable to read actual result set value in row " + rowIndex + ", column "
                    + columnIndex + ": " + exception.getMessage(), exception);
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
        LOGGER.warning(() -> ExaError.messageBuilder("W-HRM-1").message(
                "Reading a timestamp or date value without configured calendar. That's dangerous since the JDBC driver is using the time-zone of the test system in that case.")
                .mitigation(
                        "You can fix this by providing a calendar using 'withCalendar(Calendar)'. For example 'Calendar.getInstance(TimeZone.getTimeZone(\"UTC\"))'.")
                .toString());
        this.isCalendarWarningDisplayed = true;
    }

    private boolean matchCell(final Object value, final Matcher<?> cellMatcher, final int rowIndex,
            final int columnIndex) {
        if (cellMatcher.matches(value)) {
            return true;
        } else {
            this.contentDeviates = true;
            this.deviationStartRow = rowIndex;
            this.deviationStartColumn = columnIndex;
            cellMatcher.describeTo(this.cellDescription);
            cellMatcher.describeMismatch(value, this.cellMismatchDescription);
            return false;
        }
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

        /**
         * Add a column to the the structure to be matched.
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
            this.typeMatchMode = TypeMatchMode.STRICT;
            return new ResultSetStructureMatcher(this);
        }

        /**
         * Create a new matcher that matches cell types strictly.
         * 
         * @param typeMatchMode mode for type matching
         * @return matcher
         */
        public Matcher<ResultSet> matches(final TypeMatchMode typeMatchMode) {
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
