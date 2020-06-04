package com.exasol.matcher;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Hamcrest matcher that compares JDBC result sets against Java object structures.
 * <p>
 * The matcher supports strict type matching and a fuzzy mode. In fuzzy mode it value matches are accepted if a the
 * matcher knows how to convert between the expected type and the actual and the converted value matches.
 * </p>
 */
public class ResultSetStructureMatcher extends TypeSafeMatcher<ResultSet> {
    private final List<List<Object>> expectedTable;
    private final List<Column> expectedColumns;
    private int actualRowCount;
    private boolean contentDeviates;
    private int deviationStartColumn;
    private int deviationStartRow;
    private Object actualCellValue;
    private final List<Column> actualColumns = new ArrayList<>();
    private String actualCellValueJavaType;
    private final boolean fuzzy;
    private final CellMatcher cellMatcher;

    private ResultSetStructureMatcher(final Builder builder) {
        this.expectedTable = builder.expectedTable;
        this.expectedColumns = builder.expectedColumns;
        this.fuzzy = builder.fuzzy;
        this.cellMatcher = this.fuzzy ? new FuzzyCellMatcher() : new StrictCellMatcher();
        this.contentDeviates = false;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("ResultSet with ") //
                .appendValue(this.expectedTable.size()) //
                .appendText(" rows and ") //
                .appendValue(getExpectedColumnCount()) //
                .appendText(" columns");
        if (isAnyColumnDetailSpecified()) {
            description.appendList(" (", ", ", ")", this.expectedColumns);
        }
        if (this.fuzzy) {
            description.appendText(" (fuzzy match)");
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
            final Object expectedValue = this.expectedTable.get(this.deviationStartRow - 1)
                    .get(this.deviationStartColumn - 1);
            mismatchDescription.appendText(" where content deviates starting row ") //
                    .appendValue(this.deviationStartRow) //
                    .appendText(", column ") //
                    .appendValue(this.deviationStartColumn) //
                    .appendText(" with value ") //
                    .appendValue(this.actualCellValue) //
                    .appendText(" (") //
                    .appendText(this.actualCellValueJavaType) //
                    .appendText(") instead of ") //
                    .appendValue(expectedValue) //
                    .appendText(" (").appendText(expectedValue.getClass().getName()) //
                    .appendText(")");
        }
    }

    @Override
    protected boolean matchesSafely(final ResultSet resultSet) {
        boolean ok = matchColumns(resultSet);
        try {
            int rowIndex = 0;
            for (final List<Object> expectedRow : this.expectedTable) {
                if (resultSet.next()) {
                    ++rowIndex;
                    ok = ok && matchValuesInRowMatch(resultSet, rowIndex, expectedRow);
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
            final List<Object> expectedRow) {
        int columnIndex = 0;
        try {
            for (final Object expectedValue : expectedRow) {
                ++columnIndex;
                final Object value = resultSet.getObject(columnIndex);
                if (!matchCell(value, expectedValue, rowIndex, columnIndex)) {
                    return false;
                }
            }
        } catch (final SQLException exception) {
            throw new AssertionError("Unable to read actual result set value in row " + rowIndex + ", column "
                    + columnIndex + ": " + exception.getMessage());
        }
        return true;
    }

    private boolean matchCell(final Object value, final Object expectedValue, final int rowIndex,
            final int columnIndex) {
        if (this.cellMatcher.match(value, expectedValue)) {
            return true;
        } else {
            this.contentDeviates = true;
            this.deviationStartRow = rowIndex;
            this.deviationStartColumn = columnIndex;
            this.actualCellValue = value;
            this.actualCellValueJavaType = value.getClass().getName();
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
        final List<Column> expectedColumns = new ArrayList<>(types.length);
        for (final String type : types) {
            expectedColumns.add(Column.column(type));
        }
        return new Builder(expectedColumns);
    }

    /**
     * Builder for {@link ResultSetStructureMatcher} objects.
     *
     */
    public static final class Builder {
        private final List<List<Object>> expectedTable = new ArrayList<>();
        private int rows = 0;
        private List<Column> expectedColumns = null;
        private boolean fuzzy = false;

        public Builder() {
        }

        public Builder(final List<Column> expectedColumns) {
            this.expectedColumns = expectedColumns;
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
            if (this.expectedColumns == null) {
                setColumnCountExpectation(length);
            } else {
                validateColumnCount(length);
            }
            this.expectedTable.add(Arrays.asList(cellValues));
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
         * Create a new matcher that matches cell types strictly.
         *
         * @return matcher
         */
        public Matcher<ResultSet> matches() {
            return new ResultSetStructureMatcher(this);
        }

        /**
         * Create a new matcher that matches cell types fuzzily.
         *
         * @return matcher
         */
        public Matcher<ResultSet> matchesFuzzily() {
            this.fuzzy = true;
            return new ResultSetStructureMatcher(this);
        }
    }
}