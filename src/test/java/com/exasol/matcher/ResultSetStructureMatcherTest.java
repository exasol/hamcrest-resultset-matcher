package com.exasol.matcher;

import static com.exasol.matcher.ResultSetStructureMatcher.table;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.*;

import org.hamcrest.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.exasol.matcher.ResultSetStructureMatcher.Builder;

class ResultSetStructureMatcherTest extends AbstractResultSetMatcherTest {
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

    @Test
    void testDetectTooManyRows() {
        execute("CREATE TABLE TO_MANY_ROWS(COL1 VARCHAR(20), COL2 INTEGER)");
        execute("INSERT INTO TO_MANY_ROWS VALUES ('foo', 1), ('bar', 2)");
        assertQueryResultNotMatched("SELECT * FROM TO_MANY_ROWS", table().row("foo", 1).matches(),
                "ResultSet with <1> rows and <2> columns", "ResultSet with <2> rows and <2> columns");
    }

    private void assertQueryResultNotMatched(final String sql, final Matcher<ResultSet> matcher,
            final String expectedMessage, final String actualMessage) {
        final ResultSet resultSet = query(sql);
        assertThat(resultSet, not(matcher));
        final Description expectedDescription = new StringDescription();
        matcher.describeTo(expectedDescription);
        assertThat("expectation message", expectedDescription.toString(), equalTo(expectedMessage));
        final Description mismatchDescription = new StringDescription();
        matcher.describeMismatch(resultSet, mismatchDescription);
        assertThat("mismatch message", mismatchDescription.toString(), equalTo(actualMessage));
    }

    @Test
    void testDetectTooFewRows() {
        execute("CREATE TABLE TO_FEW_ROWS(COL1 VARCHAR(20), COL2 INTEGER)");
        execute("INSERT INTO TO_FEW_ROWS VALUES ('foo', 1), ('bar', 2)");
        assertQueryResultNotMatched("SELECT * FROM TO_FEW_ROWS",
                table().row("foo", 1).row("bar", 2).row("baz", 3).matches(), "ResultSet with <3> rows and <2> columns",
                "ResultSet with <2> rows and <2> columns");
    }

    @Test
    void testDetectCellValueMismatch() {
        execute("CREATE TABLE CELL_VALUE_MISMATCH(COL1 VARCHAR(20), COL2 INTEGER)");
        execute("INSERT INTO CELL_VALUE_MISMATCH VALUES ('foo', 1), ('error_here', 2)");
        assertQueryResultNotMatched("SELECT * FROM CELL_VALUE_MISMATCH", table().row("foo", 1).row("bar", 2).matches(),
                "ResultSet with <2> rows and <2> columns", "ResultSet with <2> rows and <2> columns" //
                        + " where content deviates starting row <2>, column <1>: expected was (an instance of java.lang.String and a value equal to \"bar\") but  was \"error_here\"");
    }

    @Test
    void testDetectCellTypeMismatch() {
        execute("CREATE TABLE CELL_TYPE_MISMATCH(COL1 VARCHAR(20), COL2 DECIMAL(2,0))");
        execute("INSERT INTO CELL_TYPE_MISMATCH VALUES ('foo', 1), ('bar', 2)");
        assertQueryResultNotMatched("SELECT * FROM CELL_TYPE_MISMATCH", table().row("foo", 1).row("bar", 2).matches(),
                "ResultSet with <2> rows and <2> columns", "ResultSet with <2> rows and <2> columns" //
                        + " where content deviates starting row <1>, column <2>: " //
                        + "expected was (an instance of java.lang.Integer and a value equal to <1>) but <1> is a java.math.BigDecimal");
    }

    @Test
    void testDetectColumnCountMismatchDuringRowDefinition() {
        final Builder table = table().row("foo");
        final AssertionError error = assertThrows(AssertionError.class, () -> table.row("bar", 1));
        assertThat(error.getMessage(), equalTo("Error constructing expected row 2. Expected 1 columns, but got 2."));
    }

    @Test
    void testDetectColumnCountMismatch() {
        execute("CREATE TABLE COLUMN_COUNT_MISMATCH(COL1 VARCHAR(20), COL2 INTEGER, COL3 BOOLEAN)");
        execute("INSERT INTO COLUMN_COUNT_MISMATCH VALUES ('foo', 1, true), ('bar', 2, false)");
        assertQueryResultNotMatched("SELECT * FROM COLUMN_COUNT_MISMATCH",
                table().row("foo", 1).row("bar", 2).matches(), "ResultSet with <2> rows and <2> columns",
                "ResultSet with <2> rows and <3> columns");
    }

    @Test
    void testDetectColumnTypeMismatch() {
        execute("CREATE TABLE COLUMN_TYPE_MISMATCH(COL1 VARCHAR(20), COL2 INTEGER, COL3 BOOLEAN)");
        execute("INSERT INTO COLUMN_TYPE_MISMATCH VALUES ('foo', 1, true), ('bar', 2, false)");
        assertQueryResultNotMatched("SELECT * FROM COLUMN_TYPE_MISMATCH",
                table("VARCHAR", "INTEGER", "DATE").row("foo", 1, true).row("bar", 2, false).matches(),
                "ResultSet with <2> rows and <3> columns (VARCHAR, INTEGER, DATE)",
                "ResultSet with <2> rows and <3> columns (VARCHAR, INTEGER, BOOLEAN)");
    }

    @Test
    void testNestedDateMatcher() {
        assertThat(query("VALUES VARCHAR(CURRENT_DATE)"),
                table().row(matchesPattern("\\d{4}-\\d{2}-\\d{2}")).matches());
    }

    @Test
    void testNestedAnythingMatcher() {
        assertThat(query("VALUES VARCHAR(CURRENT_DATE)"), table().row(anything()).matches());
    }

    @Test
    void testDetectCellValueFuzzyMismatch() {
        execute("CREATE TABLE CELL_VALUE_FUZZY_MISMATCH(COL1 VARCHAR(20), COL2 INTEGER)");
        execute("INSERT INTO CELL_VALUE_FUZZY_MISMATCH VALUES ('foo', 1), ('error_here', 2)");
        assertQueryResultNotMatched("SELECT * FROM CELL_VALUE_FUZZY_MISMATCH",
                table().row("foo", 1).row("bar", 2).matches(TypeMatchMode.UPCAST_ONLY),
                "ResultSet with <2> rows and <2> columns", //
                "ResultSet with <2> rows and <2> columns" //
                        + " where content deviates starting row <2>, column <1>: expected was (type that can safely be cast to java.lang.String and a value equal to \"bar\") but  was \"error_here\"");
    }

    @Test
    void testResultSetMatcherWithNoColumns() {
        execute("CREATE TABLE SIMPLE_TABLE_ONE_COLUMN(COL1 VARCHAR(20))");
        final ResultSet expected = query("SELECT * FROM SIMPLE_TABLE_ONE_COLUMN");
        assertFalse(table().matches().matches(expected));
    }
}