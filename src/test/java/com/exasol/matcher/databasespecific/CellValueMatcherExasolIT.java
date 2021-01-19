package com.exasol.matcher.databasespecific;

import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.*;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.exasol.containers.ExasolContainer;
import com.exasol.matcher.AbstractCellValueMatcherTest;

@Tag("integration")
@Testcontainers
class CellValueMatcherExasolIT extends AbstractCellValueMatcherTest {

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
    void testMatchIntegerToJavaInteger() {
        assertTypeStrictMatch("INTEGER", "1001", 1001L);
    }

    @Test
    void testMatchFloatToJavaFloatWithDefaultTolerance() {
        assertTypeFuzzyMatch("FLOAT", "2.71", 2.71);
    }

    @Override
    protected String getTestTableName() {
        return "TEST.T";
    }
}
