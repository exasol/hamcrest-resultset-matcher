package com.exasol.matcher;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CellValueMatcherTest extends AbstractCellValueMatcherTest {
    @BeforeEach
    void beforeEach() throws SQLException {
        final Connection connection = DriverManager.getConnection("jdbc:derby:memory:test;create=true");
        this.statement = connection.createStatement();
    }

    @AfterEach
    void afterEach() {
        execute("DROP TABLE T");
    }

    @Override
    protected String getTestTableName() {
        return "T";
    }

    @Test
    void testMatchIntegerToJavaInteger() {
        assertTypeStrictMatch("INTEGER", "1001", 1001);
    }
}