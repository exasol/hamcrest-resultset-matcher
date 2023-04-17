package com.exasol.matcher;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class AbstractResultSetMatcherTest {
    protected Statement statement;

    protected void execute(final String sql) {
        try {
            this.statement.execute(sql);
        } catch (final SQLException exception) {
            throw new AssertionError("Unable to execute SQL statement: " + sql, exception);
        }
    }

    protected ResultSet query(final String sql) {
        try {
            return this.statement.executeQuery(sql);
        } catch (final SQLException exception) {
            throw new AssertionError("Unable to run query: " + sql, exception);
        }
    }
}