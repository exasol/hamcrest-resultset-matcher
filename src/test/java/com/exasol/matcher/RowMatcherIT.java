package com.exasol.matcher;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
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
    void testExpectingThreeColumnsThrowsAssertionErrorWhenResultSetHasOnlyTwoColumns() {
        execute("CREATE TABLE T(I INTEGER, V VARCHAR(20))");
        execute("INSERT INTO T VALUES (1, 'a'), (2, 'b')");
        AssertionError error = assertThrows(AssertionError.class, () -> ResultSetStructureMatcher //
                .table()//
                .row(1, "a", 1) //
                .row(greaterThan(0), "b", 3) //
                .matchesInAnyOrder() //
                .matches(query("SELECT * FROM T")));
        assertThat(error.getMessage(), startsWith("Row expectation definition 2 tries to validate the value of row 1, "
                + "column 3 but that value can't be read from the result set. "
                + "This usually means the column does not exist. \nCaused by SQL error:"));
    }
}