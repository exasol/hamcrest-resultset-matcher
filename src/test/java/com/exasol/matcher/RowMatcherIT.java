package com.exasol.matcher;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This integration test runs row matching tests against the Apache Derby database.
 */
class RowMatcherIT extends AbstractResultSetMatcherTest {
    @BeforeEach
    void beforeEach() throws SQLException {
        final Connection connection = DriverManager.getConnection("jdbc:derby:memory:test;create=true");
        this.statement = connection.createStatement();
    }

    @AfterEach
    void afterEach() {
        execute("DROP TABLE T");
    }

    // This is a regression test for https://github.com/exasol/hamcrest-resultset-matcher/issues/44
    @Test
    void testMatchingInAnyOrderAndExpectingThreeColumnsThrowsAssertionErrorWhenResultSetHasOnlyTwoColumns()
            throws SQLException {
        execute("CREATE TABLE T(I INTEGER, V VARCHAR(20))");
        execute("INSERT INTO T VALUES (1, 'a'), (2, 'b')");
        final Matcher<ResultSet> anyOrderMatcher = ResultSetStructureMatcher //
                .table()//
                .row(1, "a", 1) //
                .row(greaterThan(0), "b", 3) //
                .matchesInAnyOrder();
        try(final ResultSet result = query("SELECT * FROM T")) {
            final AssertionError error = assertThrows(AssertionError.class,
                    () -> anyOrderMatcher.matches(result));
            assertThat(error.getMessage(), startsWith("Row expectation definition 1 tries to validate the value of row 1, "
                    + "column 3 but that value can't be read from the result set. "
                    + "This usually means the column does not exist. \nCaused by SQL error:"));
        }
    }

    // This is a regression test for https://github.com/exasol/hamcrest-resultset-matcher/issues/44
    @Test
    void testMatchingInStrictOrderAndExpectingThreeColumnsThrowsAssertionErrorWhenResultSetHasOnlyTwoColumns()
            throws SQLException {
        execute("CREATE TABLE T(I INTEGER, V VARCHAR(20))");
        execute("INSERT INTO T VALUES (1, 'A'), (2, 'B')");
        final Matcher<ResultSet> orderedMatcher = ResultSetStructureMatcher //
                .table()//
                .row(1, "A", 1) //
                .row(greaterThan(0), "B", 3) //
                .matches();
        try (final ResultSet result = query("SELECT * FROM T")) {
            final AssertionError error = assertThrows(AssertionError.class, () -> orderedMatcher.matches(result));
            assertThat(error.getMessage(), startsWith("Row expectation definition 1 tries to validate the value of row 1, "
                    + "column 3 but that value can't be read from the result set. "
                    + "This usually means the column does not exist. \nCaused by SQL error:"));

        }
    }
}