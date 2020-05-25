package com.exasol.matcher;

import java.sql.*;

public class AbstractResultSetMatcherTest {
    protected Statement statement;

    protected void execute(final String sql) {
        try {
            this.statement.execute(sql);
        } catch (final SQLException execption) {
            throw new AssertionError("Unable to execute SQL statement: " + sql, execption);
        }
    }

    protected ResultSet query(final String sql) {
        try {
            return this.statement.executeQuery(sql);
        } catch (final SQLException exeption) {
            throw new AssertionError("Unable to run query: " + sql, exeption);
        }
    }
}