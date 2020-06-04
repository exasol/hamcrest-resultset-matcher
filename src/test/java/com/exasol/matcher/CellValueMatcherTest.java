package com.exasol.matcher;

import static com.exasol.matcher.ResultSetStructureMatcher.table;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.exasol.matcher.ResultSetStructureMatcher.Builder;

class CellValueMatcherTest extends AbstractResultSetMatcherTest {
    @BeforeEach
    void beforeEach() throws SQLException {
        final Connection connection = DriverManager.getConnection("jdbc:derby:memory:test;create=true");
        this.statement = connection.createStatement();
    }

    @AfterEach
    void afterEach() {
        execute("DROP TABLE T");
    }

    @Test
    void testMatchIntegerToJavaInteger() {
        assertTypeStrictMatch("INTEGER", "1001", 1001);
    }

    private void assertTypeStrictMatch(final String columnType, final String actualValueSql,
            final Object expectedValue) {
        assertTypeMatch(columnType, actualValueSql, expectedValue, false);
    }

    private void assertTypeFuzzyMatch(final String columnType, final String actualValueSql,
            final Object expectedValue) {
        assertTypeMatch(columnType, actualValueSql, expectedValue, true);
    }

    private void assertTypeMatch(final String columnType, final String actualValueSql, final Object expectedValue,
            final boolean fuzzy) {
        execute("CREATE TABLE T(C1 " + columnType + ")");
        execute("INSERT INTO T VALUES (" + actualValueSql + ")");
        final Builder row = table().row(expectedValue);
        if (fuzzy) {
            assertThat(query("SELECT * FROM T"), row.matchesFuzzily());
        } else {
            assertThat(query("SELECT * FROM T"), row.matches());
        }
    }

    private void assertTypeFuzzyMismatch(final String columnType, final String actualValueSql,
            final Object expectedValue) {
        assertThrows(AssertionError.class, () -> assertTypeMatch(columnType, actualValueSql, expectedValue, true));
    }

    @Test
    void testFuzzyMatchDecimalToByteShort() {
        assertTypeFuzzyMatch("DECIMAL(20,0)", "10", (byte) 10);
    }

    @Test
    void testFuzzyMatchDecimalToJavaShort() {
        assertTypeFuzzyMatch("DECIMAL(20,0)", "300", (short) 300);
    }

    @Test
    void testFuzzyMatchDecimalToJavaInteger() {
        assertTypeFuzzyMatch("DECIMAL(20,0)", "1002", 1002);
    }

    @Test
    void testFuzzyMatchDecimalToJavaLong() {
        assertTypeFuzzyMatch("DECIMAL(20,0)", "1002", 1002L);
    }

    @Test
    void testFuzzyMatchDecimalToFloat() {
        assertTypeFuzzyMatch("DECIMAL(20,1)", "27.3", 27.3f);
    }

    @Test
    void testFuzzyMatchDecimalToDouble() {
        assertTypeFuzzyMatch("DECIMAL(20,1)", "27.3", 27.3d);
    }

    @Test
    void testFuzzyMatchDecimalWithFractionToInteger() {
        assertTypeFuzzyMismatch("DECIMAL(20,1)", "27.3", 27);
    }

    @Test
    void testFuzzyMatchDecimalWithFractionToLong() {
        assertTypeFuzzyMismatch("DECIMAL(20,1)", "27.3", 27);
    }
}