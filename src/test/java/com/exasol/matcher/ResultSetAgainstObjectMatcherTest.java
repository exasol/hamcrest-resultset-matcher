package com.exasol.matcher;

import static com.exasol.matcher.ResultSetStructureMatcher.table;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResultSetAgainstObjectMatcherTest {
    private Statement statement;

    @BeforeEach
    void beforeEach() throws SQLException {
        final Connection connection = DriverManager.getConnection("jdbc:derby:memory:test;create=true");
        this.statement = connection.createStatement();
    }

    @Test
    void testMatchSimpleResultset() {
        execute("CREATE TABLE SIMPLE(COL1 VARCHAR(20), COL2 INTEGER)");
        execute("INSERT INTO SIMPLE VALUES ('foo', 1), ('bar', 2)");
        assertThat(query("SELECT * FROM SIMPLE"), table().row("foo", 1).row("bar", 2).matches());
    }

    private void execute(final String sql) {
        try {
            this.statement.execute(sql);
        } catch (final SQLException execption) {
            throw new AssertionError("Unable to execute SQL statement: " + sql, execption);
        }
    }

    private ResultSet query(final String sql) {
        try {
            return this.statement.executeQuery(sql);
        } catch (final SQLException execption) {
            throw new AssertionError("Unable to run query: " + sql, execption);
        }
    }

    @Test
    void testDetectTooManyRows() {
        execute("CREATE TABLE TO_MANY_ROWS(COL1 VARCHAR(20), COL2 INTEGER)");
        execute("INSERT INTO TO_MANY_ROWS VALUES ('foo', 1), ('bar', 2)");
        assertQueryResultNotMatched("SELECT * FROM TO_MANY_ROWS", table().row("foo", 1).matches(),
                "more than the expected 1 rows");
    }

    private void assertQueryResultNotMatched(final String sql, final Matcher<ResultSet> matcher,
            final String expectedMessage) {
        final ResultSet resultSet = query(sql);
        assertThat(resultSet, not(matcher));
        final Description description = new StringDescription();
        matcher.describeTo(description);
        assertThat(description.toString(), containsString(expectedMessage));
    }

    @Test
    void testDetectTooFewRows() {
        execute("CREATE TABLE TO_FEW_ROWS(COL1 VARCHAR(20), COL2 INTEGER)");
        execute("INSERT INTO TO_FEW_ROWS VALUES ('foo', 1), ('bar', 2)");
        assertQueryResultNotMatched("SELECT * FROM TO_FEW_ROWS",
                table().row("foo", 1).row("bar", 2).row("baz", 3).matches(),
                "Expected result set ot have 3 rows, but it only had 2.");
    }

    @Test
    void testDetectCellValueMismatch() {
        execute("CREATE TABLE CELL_MISMATCH(COL1 VARCHAR(20), COL2 INTEGER)");
        execute("INSERT INTO CELL_MISMATCH VALUES ('foo', 1), ('error_here', 2)");
        assertQueryResultNotMatched("SELECT * FROM CELL_MISMATCH", table().row("foo", 1).row("bar", 2).matches(),
                "Result deviates in row 2, column 1. Expected: 'bar' But was: 'error_here'.");
    }

    @Test
    void testDetectColumnCountMismatchDuringRowDefinition() {
        final AssertionError error = assertThrows(AssertionError.class, () -> table().row("foo").row("bar", 1));
        assertThat(error.getMessage(), equalTo("Error constructing expected row 2. Expected 1 columns, but got 2."));
    }

    @Test
    void testDetectColumnCountMismatch() {
        execute("CREATE TABLE COLUMN_COUNT_MISMATCH(COL1 VARCHAR(20), COL2 INTEGER, COL3 BOOLEAN)");
        execute("INSERT INTO COLUMN_COUNT_MISMATCH VALUES ('foo', 1, true), ('bar', 2, false)");
        assertQueryResultNotMatched("SELECT * FROM COLUMN_COUNT_MISMATCH",
                table().row("foo", 1).row("bar", 2).matches(), "");
    }
}