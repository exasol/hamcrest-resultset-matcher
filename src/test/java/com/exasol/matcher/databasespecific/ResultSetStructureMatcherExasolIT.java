package com.exasol.matcher.databasespecific;

import static com.exasol.matcher.ResultSetStructureMatcher.table;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.sql.*;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.exasol.containers.ExasolContainer;
import com.exasol.matcher.AbstractResultSetMatcherTest;

@Testcontainers
class ResultSetStructureMatcherExasolIT extends AbstractResultSetMatcherTest {
    @Container
    private static final ExasolContainer<? extends ExasolContainer<?>> EXASOL = new ExasolContainer<>().withReuse(true)
            .withRequiredServices();

    @BeforeAll
    static void beforeAll() throws SQLException {
        final Statement statement = EXASOL.createConnection().createStatement();
        statement.executeUpdate("CREATE SCHEMA TEST;");
    }

    @BeforeEach
    void beforeEach() throws SQLException {
        this.statement = EXASOL.createConnection().createStatement();
    }

    @AfterEach
    void afterEach() {
        execute("DROP TABLE TEST.T");
    }

    @Test
    void testMatchTimestampWithCalendar() {
        execute("CREATE TABLE TEST.T(COL1 TIMESTAMP)");
        execute("INSERT INTO TEST.T VALUES (TIMESTAMP '2007-03-31 12:59:30.123')");
        final Timestamp expected = new Timestamp(1175345970123L);
        assertThat(query("SELECT * FROM TEST.T"), table() //
                .row(expected) //
                .withUtcCalendar()//
                .matches());
    }

    // Testing without calendar is dangerous, because while you might assume you are testing a bare timestamp, Java
    // actually adds the client calendar. This way match results are not guaranteed. This is why we only ensure that
    // no exception falls here.
    @Test
    void testMatchTimestampWithoutCalendar() throws SQLException {
        execute("CREATE TABLE TEST.T(COL1 TIMESTAMP)");
        execute("INSERT INTO TEST.T VALUES (TIMESTAMP '2007-03-31 12:59:30.123')");
        try (final ResultSet resultSet = query("SELECT * FROM TEST.T")) {
            assertDoesNotThrow(() -> table().row(Matchers.anything()).matches().matches(resultSet));
        }
    }

    @Test
    void testMatchDateWithCalendar() {
        execute("CREATE TABLE TEST.T(COL1 DATE)");
        execute("INSERT INTO TEST.T VALUES (DATE '2007-03-31')");
        final Date expected = new Date(1175345970123L);
        assertThat(query("SELECT * FROM TEST.T"), table() //
                .row(expected) //
                .withUtcCalendar()//
                .matches());
    }

    // Testing without calendar is dangerous, because while you might assume you are testing a bare timestamp, Java
    // actually adds the client calendar. This way match results are not guaranteed. This is why we only ensure that
    // no exception falls here.
    @Test
    void testMatchDateWithoutCalendar() throws SQLException {
        execute("CREATE TABLE TEST.T(COL1 DATE)");
        execute("INSERT INTO TEST.T VALUES (DATE '2007-03-31')");
        try (final ResultSet resultSet = query("SELECT * FROM TEST.T")) {
            assertDoesNotThrow(() -> table().row(Matchers.anything()).matches().matches(resultSet));
        }
    }
}
