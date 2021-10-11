package com.exasol.matcher;

import static com.exasol.matcher.ResultSetStructureMatcher.table;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * This class contains abstract tests for the matching of cell values.
 * <p>
 * The reason for this class being an abstract test is, that some behavior can only be reproduced using certain
 * databases, since different databases use different JDBC return types. For that reason it is important to test the
 * implementation for different databases. The {@code CellValueMatcherIT} is an integration test using the derby JDBC
 * connector. The {@link com.exasol.matcher.databasespecific} package contains the database specific integration tests.
 * </p>
 */
@SuppressWarnings("java:S5786") // this class needs to be public since it is a abstract class inherited from different
                                // packages
public abstract class AbstractCellValueMatcherTest extends AbstractResultSetMatcherTest {
    @Test
    void testMatchNull() {
        assertTypeStrictMatch("VARCHAR(39)", "NULL", null);
    }

    @Test
    void testNotMatchNull() {
        assertThrows(AssertionError.class, () -> assertTypeStrictMatch("VARCHAR(38)", "'test'", null));
    }

    protected void assertTypeStrictMatch(final String columnType, final String actualValueSql,
            final Object expectedValue) {
        assertTypeMatch(columnType, actualValueSql, expectedValue, false);
    }

    protected void assertTypeFuzzyMatch(final String columnType, final String actualValueSql,
            final Object expectedValue) {
        assertTypeMatch(columnType, actualValueSql, expectedValue, true);
    }

    /**
     * Get the fully qualified name for a table to use in tests. If the name contains a schema or catalog the schema
     * must exist.
     *
     * @return name for a table
     */
    protected abstract String getTestTableName();

    protected void assertTypeMatch(final String columnType, final String actualValueSql, final Object expectedValue,
            final boolean fuzzy) {
        final String testTableName = getTestTableName();
        execute("CREATE TABLE " + testTableName + "(C1 " + columnType + ")");
        execute("INSERT INTO " + testTableName + " VALUES (" + actualValueSql + ")");
        final ResultSetStructureMatcher.Builder row = table().row(expectedValue);
        if (fuzzy) {
            assertThat(query("SELECT * FROM " + testTableName), row.matches(TypeMatchMode.NO_JAVA_TYPE_CHECK));
        } else {
            assertThat(query("SELECT * FROM " + testTableName), row.matches());
        }
    }

    protected AssertionError assertTypeFuzzyMismatch(final String columnType, final String actualValueSql,
            final Object expectedValue) {
        return assertThrows(AssertionError.class,
                () -> assertTypeMatch(columnType, actualValueSql, expectedValue, true));
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
    void testFuzzyMatchDecimalToJavaDouble() {
        assertTypeFuzzyMatch("DECIMAL(20,0)", "300", 300.0);
    }

    @Test
    void testFuzzyMatchDecimalToJavaFloat() {
        assertTypeFuzzyMatch("DECIMAL(20,0)", "300", 300.0f);
    }

    @Test
    void testFuzzyMatchUntypicalDecimalToJavaInteger() {
        assertTypeFuzzyMatch("DECIMAL(8,2)", "1002", 1002);
    }

    @Test
    void testFuzzyMatchUntypicalDecimalToJavaLong() {
        assertTypeFuzzyMatch("DECIMAL(8,2)", "1002", 1002L);
    }

    @Test
    void testFuzzyMatchUntypicalDecimalToJavaDouble() {
        assertTypeFuzzyMatch("DECIMAL(8,2)", "1002", 1002.0);
    }

    @Test
    void testFuzzyMatchUntypicalDecimalToJavaBigDecimal() {
        assertTypeFuzzyMatch("DECIMAL(8,2)", "1002", BigDecimal.valueOf(1002));
    }

    @Test
    void testFuzzyMatchUntypicalDecimalToJavaDoubleBigDecimal() {
        assertTypeFuzzyMatch("DECIMAL(8,2)", "1002", BigDecimal.valueOf(1002.0));
    }

    @Test
    void testFuzzyMatchDecimalToFloat() {
        assertTypeFuzzyMatch("DECIMAL(20,1)", "27.3", 27.3f);
    }

    @Test
    void testFuzzyMismatchDecimalToFloat() {
        assertTypeFuzzyMismatch("DECIMAL(20,1)", "27.3", 44.8f);
    }

    @Test
    void testFuzzyMatchDecimalToDouble() {
        assertTypeFuzzyMatch("DECIMAL(20,1)", "27.3", 27.3d);
    }

    @Test
    void testFuzzyMismatchDecimalToDouble() {
        assertTypeFuzzyMismatch("DECIMAL(20,1)", "27.3", 44.8d);
    }

    @Test
    void testFuzzyMatchDecimalWithFractionToInteger() {
        assertTypeFuzzyMismatch("DECIMAL(20,1)", "27.3", 27);
    }

    @Test
    void testFuzzyMismatchDecimalWithFractionToLong() {
        assertTypeFuzzyMismatch("DECIMAL(30,1)", "27.3", 27);
    }

    @Test
    void testFuzzyMismatchDecimalDate() {
        assertTypeFuzzyMismatch("DECIMAL(20,1)", "123456789012345678.0", new Date());
    }

    @Test
    void testFuzzyMatchTwoStrings() {
        assertTypeFuzzyMatch("VARCHAR(40)", "'a'", "a");
    }

    @Test
    void testFuzzyMismatchTwoStrings() {
        assertTypeFuzzyMismatch("VARCHAR(40)", "'a'", "b");
    }

    @ParameterizedTest
    @ValueSource(strings = { "26.81", "26.83" })
    void testFuzzyMatchDecimalWithTolerance(final String actualValue) {
        assertTypeFuzzyMatch("DECIMAL(30,3)", actualValue,
                FuzzyCellMatcher.fuzzilyEqualTo(26.82, BigDecimal.valueOf(0.015)));
    }

    @Test
    void testFuzzyMismatchDecimalWithTolerance() {
        final AssertionError exception = assertTypeFuzzyMismatch("DECIMAL(30,3)", "26.81",
                FuzzyCellMatcher.fuzzilyEqualTo(26.83, BigDecimal.valueOf(0.015)));
        assertThat(exception.getMessage(), endsWith(
                "ResultSet with <1> rows and <1> columns where content deviates starting row <1>, column <1>: expected was a value close to <26.83> (tolerance: +/- <0.015>) but  was <26.810> difference was <0.020>"));
    }
}
