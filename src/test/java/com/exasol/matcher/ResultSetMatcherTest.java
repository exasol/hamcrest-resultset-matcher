package com.exasol.matcher;

import static com.exasol.matcher.ResultSetMatcher.matchesResultSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThrows;

import java.sql.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResultSetMatcherTest extends AbstractResultSetMatcherTest {
    private Connection connection;

    @BeforeEach
    void beforeEach() throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:derby:memory:test;create=true");
        this.statement = this.connection.createStatement();
    }

    @Test
    void testMatchResultSet() throws SQLException {
        execute("CREATE TABLE SIMPLE_TABLE(COL1 VARCHAR(20), COL2 INTEGER, COL3 BOOLEAN, COL4 TIMESTAMP)");
        execute("INSERT INTO SIMPLE_TABLE VALUES " //
                + "('foo', 1, true, '2020-01-01 23:03:20'), " //
                + "('bar', 2, false, '1960-01-01 23:03:20')");
        final ResultSet expected = query("SELECT * FROM SIMPLE_TABLE");
        // Derby doesn't support two opened result sets on one statement, so we create one more statement.
        final Statement statement2 = this.connection.createStatement();
        final ResultSet actual = statement2.executeQuery("SELECT * FROM SIMPLE_TABLE");
        assertThat(actual, matchesResultSet(expected));
    }

    @Test
    void testColumnsCounterMismatch() throws SQLException {
        execute("CREATE TABLE COL_COUNT_MISMATCH(COL1 VARCHAR(20), COL2 INTEGER)");
        execute("INSERT INTO COL_COUNT_MISMATCH VALUES ('foo', 1), ('bar', 2)");
        execute("CREATE TABLE COL_COUNT_MISMATCH_2(COL1 VARCHAR(20))");
        execute("INSERT INTO COL_COUNT_MISMATCH_2 VALUES ('foo'), ('bar')");
        final ResultSet expected = query("SELECT * FROM COL_COUNT_MISMATCH");
        // Derby doesn't support two opened result sets on one statement, so we create one more statement.
        final Statement statement2 = this.connection.createStatement();
        final ResultSet actual = statement2.executeQuery("SELECT * FROM COL_COUNT_MISMATCH_2");
        final AssertionError error = assertThrows(AssertionError.class,
                () -> assertThat(actual, matchesResultSet(expected)));
        assertThat(error.getMessage(), containsString("Expected: ResultSet with <2> column(s)\n" //
                + "     but: ResultSet with <1> column(s)"));
    }

    @Test
    void testRowCounterMismatch() throws SQLException {
        execute("CREATE TABLE ROW_COUNT_MISMATCH(COL1 VARCHAR(20), COL2 INTEGER)");
        execute("INSERT INTO ROW_COUNT_MISMATCH VALUES ('foo', 1), ('bar', 2)");
        execute("CREATE TABLE ROW_COUNT_MISMATCH_2(COL1 VARCHAR(20), COL2 INTEGER)");
        execute("INSERT INTO ROW_COUNT_MISMATCH_2 VALUES ('foo', 1)");
        final ResultSet expected = query("SELECT * FROM ROW_COUNT_MISMATCH");
        // Derby doesn't support two opened result sets on one statement, so we create one more statement.
        final Statement statement2 = this.connection.createStatement();
        final ResultSet actual = statement2.executeQuery("SELECT * FROM ROW_COUNT_MISMATCH_2");
        final AssertionError error = assertThrows(AssertionError.class,
                () -> assertThat(actual, matchesResultSet(expected)));
        assertThat(error.getMessage(), containsString("Expected: ResultSet with <2> row(s)\n" //
                + "     but: ResultSet with <1> row(s)"));
    }

    @Test
    void testValueMismatch() throws SQLException {
        execute("CREATE TABLE VALUE_MISMATCH(COL1 VARCHAR(20), COL2 INTEGER)");
        execute("INSERT INTO VALUE_MISMATCH VALUES ('foo', 1), ('bar', 2)");
        execute("CREATE TABLE VALUE_MISMATCH_2(COL1 VARCHAR(20), COL2 INTEGER)");
        execute("INSERT INTO VALUE_MISMATCH_2 VALUES ('foo', 1), ('bar', 100)");
        final ResultSet expected = query("SELECT * FROM VALUE_MISMATCH");
        // Derby doesn't support two opened result sets on one statement, so we create one more statement.
        final Statement statement2 = this.connection.createStatement();
        final ResultSet actual = statement2.executeQuery("SELECT * FROM VALUE_MISMATCH_2");
        final AssertionError error = assertThrows(AssertionError.class,
                () -> assertThat(actual, matchesResultSet(expected)));
        assertThat(error.getMessage(), containsString("Expected: Integer field value <2> (column 2, row 2)\n"
                + "     but: Integer field value <100> (column 2, row 2)"));
    }

    @Test
    void testDataTypeMismatch() throws SQLException {
        execute("CREATE TABLE DATA_TYPE_MISMATCH(COL1 VARCHAR(20), COL2 VARCHAR(20))");
        execute("INSERT INTO DATA_TYPE_MISMATCH VALUES ('foo', '1'), ('bar', '2')");
        execute("CREATE TABLE DATA_TYPE_MISMATCH_2(COL1 VARCHAR(20), COL2 INTEGER)");
        execute("INSERT INTO DATA_TYPE_MISMATCH_2 VALUES ('foo', 1), ('bar', 2)");
        final ResultSet expected = query("SELECT * FROM DATA_TYPE_MISMATCH");
        // Derby doesn't support two opened result sets on one statement, so we create one more statement.
        final Statement statement2 = this.connection.createStatement();
        final ResultSet actual = statement2.executeQuery("SELECT * FROM DATA_TYPE_MISMATCH_2");
        final AssertionError error = assertThrows(AssertionError.class,
                () -> assertThat(actual, matchesResultSet(expected)));
        assertThat(error.getMessage(), containsString(
                "Expected: Column <2> with JDBC Data Type 12\n" + "     but: Column <2> with JDBC Data Type 4"));
    }
}