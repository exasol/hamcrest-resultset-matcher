package com.exasol.matcher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;

import java.sql.*;

import org.junit.jupiter.api.*;

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

    @Test
    void testFuzzyUpcastOnlyMismatch() {
        final AssertionError error = assertTypeFuzzyMismatch("INTEGER", "12",
                UpcastOnlyCellMatcher.isOnlyUpcastTo((short) 12));
        assertThat(error.getMessage(), endsWith(
                "The actual type is bigger than the expected. You can disable this check by using the NO_JAVA_TYPE_CHECK fuzzy-mode."));
    }

    @Test
    void testFuzzyUpcastOnlyMismatchToFloat() {
        final AssertionError error = assertTypeFuzzyMismatch("INTEGER", "12",
                UpcastOnlyCellMatcher.isOnlyUpcastTo(12.0f));
        assertThat(error.getMessage(), endsWith(
                "Illegal upcast. Upcasts are only allowed from non floating types <= short to float and from types <= integer to double."));
    }

    @Test
    void testFuzzyUpcastOnlyMatchToDouble() {
        assertTypeFuzzyMatch("INTEGER", "12", UpcastOnlyCellMatcher.isOnlyUpcastTo(12.0d));
    }

    @Test
    void testFuzzyMatchVarcharToInt() {
        assertTypeFuzzyMatch("VARCHAR(5)", "'12'", FuzzyCellMatcher.fuzzilyEqualTo(12));
    }

    @Test
    void testFuzzyMatchIntegerToVarchar() {
        assertTypeFuzzyMatch("INTEGER", "12", FuzzyCellMatcher.fuzzilyEqualTo("12"));
    }

    @Test
    void testFuzzyMismatchVarcharToInt() {
        final AssertionError assertionError = assertTypeFuzzyMismatch("VARCHAR(5)", "'test'",
                FuzzyCellMatcher.fuzzilyEqualTo(12));
        assertThat(assertionError.getMessage(), equalTo(
                "\nExpected: ResultSet with <1> rows and <1> columns\n     but: ResultSet with <1> rows and <1> columns where content deviates starting row <1>, column <1>: expected was a value equal to <12> but  was \"test\""));
    }

    @Test
    void testFuzzyUpcastOnlyMatchToInteger() {
        assertTypeFuzzyMatch("INTEGER", "12", UpcastOnlyCellMatcher.isOnlyUpcastTo(12));
    }

    @Test
    void testFuzzyUpcastOnlyMatchToLong() {
        assertTypeFuzzyMatch("INTEGER", "12", UpcastOnlyCellMatcher.isOnlyUpcastTo(12L));
    }
}