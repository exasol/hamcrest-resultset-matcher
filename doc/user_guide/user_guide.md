# Hamcrest ResultSet Matcher User Guide

The `ResultSet` matcher is the implementation of a [Java Hamcrest matcher](JavaHamcrest). The Hamcrest (an anagram of "matchers") suite is a collection of matchers that aim to be written declaratively and provide better than average diagnostics messages in case of mismatches.

This particular matcher helps testers to validate the contents of [JDBC result sets](https://docs.oracle.com/en/java/javase/11/docs/api/java.sql/java/sql/ResultSet.html). Checking result sets is a typical part of integration testing Java database applications.

This matcher collection here makes that task more convenient.

## Getting the ResultSet Matcher Into Your Project

The Hamcrest ResultSet Matchers are built using [Apache Maven](https://maven.apache.org/), so integrating the release package into your project is easy with Maven.

Please check out ["Introduction to the Dependency Mechanism"](http://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html), if you want to learn about how maven handles dependencies and dependency scopes.

We assume here that you are familiar with the basics.

### Hamcrest ResultSet Matcher as Maven Dependency

Just add the following dependency to add the Hamcrest ResultSet Matcher to your project.

```xml
<dependency>
    <groupId>com.exasol</groupId>
    <artifactId>hamcrest-resultset-matcher</artifactId>
    <version><!-- add latest version here --></version>
    <scope>test</scope>
</dependency>
```

As always, check for the latest version of the dependencies.

## Matchers and Unit Test Frameworks

Although checking a result set is by definition already an integration test (because you are testing the integration of your software with a database), you will most likely write the tests using a unit testing framework.

We recommend using [test containers together with JUnit 5](https://www.testcontainers.org/test_framework_integration/junit_5/). If you want to do that, please also add the following dependency.

```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>1.12.5</version>
    <scope>test</scope>
</dependency>
```

Of course other unit test frameworks will also work just fine.

In our examples below we assume you are using JUnit 5 though.

## The Two Flavors of the ResulSetMatcher

Depending on your personal preferences or the use case for the test, you can pick between two different kinds of result set matchers.

1. Matcher that compares two JDBC `ResultSet`s
2. Matcher that compares a JDBC `ResultSet` against a definition written in Java

## The `ResultSetMatcher`

TODO: add

## The `ResultSetStructureMatcher`

This matcher provides a builder that lets you create a Java object representation of the expected result set.

Imagine you want to test code that fills the following table:

```sql
CREATE TABLE Customers (ID INTEGER, FIRSTNAME VARCHAR(40), LASTNAME VARCHAR(40));
```

A minimal test would then look as in the example below. We removed all parts that are non-essential for understanding the code (e.g. imports that are not matcher-related).

```java
import static com.exasol.matcher.ResultSetStructureMatcher.table;
import static org.hamcrest.MatcherAssert.assertThat;

// ...

class CustomerTablePopulationTest {
    @Test
    void testTableContents() {
        // ...
        final ResulSet result = statement.execute("SELECT * FROM CUSTOMERS");
        
        assertThat(result, table()
                .row(1, "JOHN", "DOE")
                .row(2, "JANE", "SMITH)
                .matches());
    } 
}
```

As you can see, the test validates that the result set contains two rows and those rows contents. It does however not care about the column type of the result set. If you want to make the test more strict in that respect, you can add type names to the factory method `table(...)`.

```java
assertThat(result, table("INTEGER", "VARCHAR", "VARCHAR")
        .row(1, "JOHN", "DOE")
        .row(2, "JANE", "SMITH)
        .matches());
```
