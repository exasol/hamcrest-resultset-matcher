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
 */
public class ResultSetStructureMatcher extends TypeSafeMatcher<ResultSet> {
    private final List<List<Object>> expectedTable;
    private final int expectedColumnCount;
    private String collectedDescription;

    private ResultSetStructureMatcher(final Builder builder) {
        this.expectedTable = builder.structure;
        this.expectedColumnCount = builder.columnCount;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText(this.collectedDescription);
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
                    this.collectedDescription = "Expected result set ot have " + this.expectedTable.size()
                            + " rows, but it only had " + rowIndex + ".";
                    ok = false;
                }
            }
            if (resultSet.next()) {
                this.collectedDescription = "Result table has more than the expected " + this.expectedTable.size()
                        + " rows.";
                ok = false;
            }
        } catch (final SQLException exception) {
            this.collectedDescription = "Unable to check result set: " + exception.getMessage();
            ok = false;
        }
        return ok;
    }

    private boolean matchColumns(final ResultSet resultSet) {
        ResultSetMetaData metadata;
        try {
            metadata = resultSet.getMetaData();
            return metadata.getColumnCount() == this.expectedColumnCount;
        } catch (final SQLException e) {
            this.collectedDescription = "Unable to determine result's the result set's column count.";
            return false;
        }
    }

    private boolean matchValuesInRowMatch(final ResultSet resultSet, final int rowIndex,
            final List<Object> expectedRow) {
        int columnIndex = 0;
        try {
            for (final Object expectedValue : expectedRow) {
                ++columnIndex;
                final Object value = resultSet.getObject(columnIndex);
                if (!value.equals(expectedValue)) {
                    this.collectedDescription = "Result deviates in row " + rowIndex + ", column " + columnIndex
                            + ". Expected: '" + expectedValue + "' But was: '" + value + "'.";
                    return false;
                }
            }
        } catch (final SQLException exception) {
            this.collectedDescription = "Unable to read actual result set value in row " + rowIndex + ", column "
                    + columnIndex + ": " + exception.getMessage();
            return false;
        }
        return true;
    }

    public static Builder table() {
        return new Builder();
    }

    /**
     * Builder for {@link ResultSetStructureMatcher} objects.
     *
     */
    public static final class Builder {
        private final List<List<Object>> structure = new ArrayList<>();
        private int columnCount = -1;
        private int rows = 0;

        /**
         * Add a row to the structure to be matched.
         *
         * @param cellValues values of the cells in the row
         * @return {@code this} for fluent programming
         */
        public Builder row(final Object... cellValues) {
            ++this.rows;
            if (this.columnCount < 0) {
                this.columnCount = cellValues.length;
            } else if (cellValues.length != this.columnCount) {
                throw new AssertionError("Error constructing expected row " + this.rows + ". Expected "
                        + this.columnCount + " columns, but got " + cellValues.length + ".");
            }
            this.structure.add(Arrays.asList(cellValues));
            return this;
        }

        public Matcher<ResultSet> matches() {
            return new ResultSetStructureMatcher(this);
        }
    }
}