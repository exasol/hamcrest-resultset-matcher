package com.exasol.matcher;

import static com.exasol.matcher.ResultSetStructureMatcher.table;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.sql.*;

import org.hamcrest.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.exasol.matcher.ResultSetStructureMatcher.Builder;

// Derby does not support SELECT FROM VALUES, so we have to create actual tables and insert values into them in the
// test preparation. Don't try to rewrite this with SELECT FROM VALUES.

class ResultSetStructureMatcherTest extends AbstractResultSetMatcherTest {
    @BeforeEach
    void beforeEach() throws SQLException {
        final Connection connection = DriverManager.getConnection("jdbc:derby:memory:test;create=true");
        this.statement = connection.createStatement();
    }

    @Test
    void testMatchSimpleResultSet() {
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

    @Test
    void testDetectFloatingPointMismatch() {
        execute("CREATE TABLE FLOAT_MISMATCH(COL1 FLOAT)");
        execute("INSERT INTO FLOAT_MISMATCH VALUES (3.141592653589)");
        assertQueryResultNotMatched("SELECT * FROM FLOAT_MISMATCH", table().row(3.1415926535899).matches(),
                "ResultSet with <1> rows and <1> columns", //
                "ResultSet with <1> rows and <1> columns where content deviates starting row <1>, column <1>: " //
                        + "expected was (an instance of java.lang.Double and a value equal to <3.1415926535899>) but  was <3.141592653589>");
    }

    @Test
    void testMatchWithDefaultTolerance() {
        execute("CREATE TABLE SIMPLE_FLOATS_WITH_DEFAULT_TOLERANCE(COL1 FLOAT)");
        execute("INSERT INTO SIMPLE_FLOATS_WITH_DEFAULT_TOLERANCE VALUES (2.71), (3.14)");
        assertThat(query("SELECT * FROM SIMPLE_FLOATS_WITH_DEFAULT_TOLERANCE"), table().row(2.71).row(3.14).matches());
    }

    @Test
    void testMatchWithToleranceValue() {
        execute("CREATE TABLE SIMPLE_FLOATS_WITH_TOLERANCE(COL1 FLOAT)");
        execute("INSERT INTO SIMPLE_FLOATS_WITH_TOLERANCE VALUES (26.81), (26.83)");
        assertThat(query("SELECT * FROM SIMPLE_FLOATS_WITH_TOLERANCE"), table() //
                .withDefaultNumberTolerance(BigDecimal.valueOf(0.015)) //
                .row(26.82) //
                .row(26.82) //
                .matches());
    }

    @Test
    void testMatchWithToleranceUsingCellMatcher() {
        execute("CREATE TABLE SIMPLE_FLOATS_CELL_MATCHER(COL1 FLOAT)");
        execute("INSERT INTO SIMPLE_FLOATS_CELL_MATCHER VALUES (1.34), (2.567998)");
        final BigDecimal EPS = BigDecimal.valueOf(0.015);
        assertThat(query("SELECT * FROM SIMPLE_FLOATS_CELL_MATCHER"), table() //
                .row(CellMatcherFactory.cellMatcher(1.35, TypeMatchMode.STRICT, EPS)) //
                .row(CellMatcherFactory.cellMatcher(2.567997, TypeMatchMode.STRICT, EPS)) //
                .matches());
    }

    @Test
    void testMatchInAnyOrder() {
        execute("CREATE TABLE IN_ANY_ORDER_MATCHER(COL1 VARCHAR(10), COL2 INTEGER)");
        execute("INSERT INTO IN_ANY_ORDER_MATCHER VALUES ('first', 1), ('second', 2), ('third', 3)");
        assertThat(query("SELECT * FROM IN_ANY_ORDER_MATCHER"), table() //
                .row("second", 2) //
                .row("third", 3) //
                .row("first", 1).matchesInAnyOrder());
    }

    @Test
    void testMatchInAnyOrderWithNestedMatcher() {
        execute("CREATE TABLE IN_ANY_ORDER_MATCHER_NESTED(COL1 VARCHAR(10), COL2 INTEGER)");
        execute("INSERT INTO IN_ANY_ORDER_MATCHER_NESTED VALUES ('first', 1), ('second', 2), ('third', 3)");
        assertThat(query("SELECT * FROM IN_ANY_ORDER_MATCHER_NESTED"), table() //
                .row(containsString("con"), 2) //
                .row("third", greaterThan(2)) //
                .row("first", 1).matchesInAnyOrder());
    }

    @Test
    void testMatchInAnyOrderFailsBecauseTooManyRows() {
        execute("CREATE TABLE IN_ANY_ORDER_MATCHER_TOO_MANY_ROWS(COL1 VARCHAR(10), COL2 INTEGER)");
        execute("INSERT INTO IN_ANY_ORDER_MATCHER_TOO_MANY_ROWS VALUES ('first', 1), ('second', 2), ('third', 3)");
        assertQueryResultNotMatched("SELECT * FROM IN_ANY_ORDER_MATCHER_TOO_MANY_ROWS", //
                table().row("second", 2).row("third", 3).matchesInAnyOrder(), //
                "ResultSet with <2> rows and <2> columns", //
                "ResultSet with <3> rows and <2> columns"
                        + " where row <1> was the first that did not match any expected row");
    }

    @Test
    void testMatchInAnyOrderFailsBecauseTooFewRows() {
        execute("CREATE TABLE IN_ANY_ORDER_MATCHER_TOO_FEW_ROWS(COL1 VARCHAR(10), COL2 INTEGER)");
        execute("INSERT INTO IN_ANY_ORDER_MATCHER_TOO_FEW_ROWS VALUES ('first', 1), ('second', 2)");
        assertQueryResultNotMatched("SELECT * FROM IN_ANY_ORDER_MATCHER_TOO_FEW_ROWS", //
                table().row("second", 2).row("third", 3).row("first", 1).matchesInAnyOrder(),
                "ResultSet with <3> rows and <2> columns", //
                "ResultSet with <2> rows and <2> columns");
    }

    @Test
    void testMatchInAnyOrderFailsBecauseOfUnmatchedRow() {
        execute("CREATE TABLE IN_ANY_ORDER_MATCHER_UNMATCHED_ROW(COL1 VARCHAR(10), COL2 INTEGER)");
        execute("INSERT INTO IN_ANY_ORDER_MATCHER_UNMATCHED_ROW VALUES ('Moe', 1), ('Larry', 2), ('Curly', 3)");
        assertQueryResultNotMatched("SELECT * FROM IN_ANY_ORDER_MATCHER_UNMATCHED_ROW", //
                table().row("Larry", 2).row("Curly", 777).row("Moe", 1).matchesInAnyOrder(),
                "ResultSet with <3> rows and <2> columns", //
                "ResultSet with <3> rows and <2> columns"
                        + " where row <3> was the first that did not match any expected row");
    }

    @Test
    void testMatchInAnyOrderFailsBecauseOfAmbiguousMatch() {
        execute("CREATE TABLE IN_ANY_ORDER_MATCHER_AMBIGUOUS(COL1 VARCHAR(10))");
        execute("INSERT INTO IN_ANY_ORDER_MATCHER_AMBIGUOUS VALUES ('Tic'), ('Tac'), ('Toe')");
        assertQueryResultNotMatched("SELECT * FROM IN_ANY_ORDER_MATCHER_AMBIGUOUS", //
                table().row("Toe").row("Tac").row(matchesPattern("T.c")).matchesInAnyOrder(),
                "ResultSet with <3> rows and <1> columns", //
                "ResultSet with <3> rows and <1> columns"
                        + " where at least one expected row matched multiple result rows."
                        + " Please narrow down the matching criteria to avoid ambiguity.");
    }

    @Test
    void testMatchInAnyOrderFailsBecauseOfTypeMismatch() {
        execute("CREATE TABLE IN_ANY_ORDER_MATCHER_TYPE_MISMATCH(COL1 VARCHAR(10))");
        execute("INSERT INTO IN_ANY_ORDER_MATCHER_TYPE_MISMATCH VALUES ('Tic'), ('Tac'), ('Toe')");
        assertQueryResultNotMatched("SELECT * FROM IN_ANY_ORDER_MATCHER_TYPE_MISMATCH", //
                table("CHAR(10)").row("Toe").row("Tac").row(matchesPattern("Tic")).matchesInAnyOrder(),
                "ResultSet with <3> rows and <1> columns (CHAR(10))", //
                "ResultSet with <3> rows and <1> columns (VARCHAR)");
    }

    @Test
    void testMatchInAnyOrderWithNestedMatcherWithTypeCheckMode() {
        execute("CREATE TABLE IN_ANY_ORDER_MATCHER(COL1 VARCHAR(10), COL2 INTEGER)");
        execute("INSERT INTO IN_ANY_ORDER_MATCHER VALUES ('first', 1), ('second', 2), ('third', 3)");
        assertThat(query("SELECT * FROM IN_ANY_ORDER_MATCHER"), table() //
                .row("second", (long) 2) //
                .row("third", (byte) 3) //
                .row("first", (int) 1).matchesInAnyOrder(TypeMatchMode.NO_JAVA_TYPE_CHECK));
    }
}
