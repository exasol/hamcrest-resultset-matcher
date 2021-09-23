# Hamcrest ResultSet Matcher User Guide

The `ResultSet` matcher is the implementation of a [Java Hamcrest matcher](http://hamcrest.org/JavaHamcrest/). The Hamcrest (an anagram of "matchers") suite is a collection of matchers that aim to be written declaratively and provide better than average diagnostics messages in case of mismatches.

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

This matcher allows you to compare two `ResultSet`s. This can be helpful when you need, for example, to compare each value of two tables.

A minimal test would then look as in the example below.

```java
import java.sql.ResultSet;

import static org.hamcrest.MatcherAssert.assertThat;

// ...

class CustomerTablePopulationTest {
    @Test
    void testTableContents() {
        // ...
        final ResulSet table1 = statement1.execute("SELECT * FROM CUSTOMERS");
        final ResultSet table2 = statement2.execute("SELECT * FROM CUSTOMERS2");

        assertThat(table1, matchesResultSet(table2));
    }
}
```

Please keep in mind that you need to have two opened `ResultSet`s for this matcher. Some JDBC drivers close the previously opened `ResultSet` as soon as you execute the next query on a `Statement`. So you might be need to have two statements as shown in the example above.

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
                .row(2, "JANE", "SMITH")
                .matches());
    }
}
```

As you can see, the test validates that the result set contains two rows and those rows contents. It does however not care about the column type of the result set. If you want to make the test more strict in that respect, you can add type names to the factory method `table(...)`.

```java
assertThat(result,table("INTEGER","VARCHAR","VARCHAR")
        .row(1,"JOHN","DOE")
        .row(2,"JANE","SMITH")
        .matches());
```

### Type Checks / Fuzzy Matching

By default, the `ResultSetStructureMatcher` checks that the Java data types of the result matches the ones you specified.

In the regular `STRICT` mode, the Java types in the result set and in the definition of your expectation must match exactly. While this can be what you want, there are also cases where exact matches are simply too much effort for what you actually want to test. For that there `ResultSetStructureMatcher` has two other type matching modes: `NO_JAVA_TYPE_CHECK`
and `UPCAST_ONLY`

#### The `NO_JAVA_TYPE_CHECK` Type Check Mode

Imagine a case where the result set contains a `DECIMAL(2,0)` column. The corresponding Java type is `BigDecimal`. So if you want to do a strict match, you need to say:

```java
assertThat(result,table("DECIMAL")
        .row(BigDecimal.valueOf(1234))
        .row(BigDecimal.valueOf(987654321)
        // ...
        .matches()); // alias for .matches(TypeMatchMode.STRICT)
```

That's very explicit. In fact it is probably a lot more explicit than you are comfortable with &mdash; especially if you want to compare a lot of rows.

This is where fuzzy matching comes into play.

What you probably want to test is if the integer value 1234 matches the value of the first cell in row one and so on. To do this, you can instead formulate the following assertion:

```java
assertThat(result,
        table("DECIMAL")
        .row(1234)
        .row(987654321)
        // ...
        .matches(TypeMatchMode.NO_JAVA_TYPE_CHECK));
```

This also matches numbers and strings if they have the same value. For example it considers 1 and "1" as equal.

#### The `UPCAST_ONLY` Type Check Mode

If you don't want to have the strict type checks but still need some type safety you can use the `UPCAST_ONLY` mode. In this mode, the `ResultSetStructureMatcher` will check if the actual data type fits into the one you defined.

For example getting a `Short` instead of an `Integer` is acceptable because `Integer` is a larger type than `Short`.

The `ResultSetStructureMatcher` does not allow `Float`s and `Double`s values for expected non-floating-point numbers. The other way around it is fine &mdash; as long as the type fit's into the other one (types &le; `Short` &rarr; `Float` and &ge; `Integer` into `Double`).

### Nesting Matchers

Comparing result sets against tables full of constants is fine if the result is perfectly deterministic and you are actually interested in all results.

Imagine a situation where you have an integration test with a random value column (e.g. a password salt). In a case like this you can check if the result has the right type and range, but not if the actual value is as expected.

In the example below we have a table containing the following columns:

1. user name
1. randomly generated and encrypted password encoded as hexadecimal string of varying length
1. numeric salt of arbitrary size, but never negative

```java
assertThat(result,
        table()
        .row("fred",matchesPattern("[0-9A-F]+"),greaterThanOrEqualTo(0))
        .matches();
```

Of course you can nest matchers in the nested matcher. That's the beauty of Hamcrest.

### Matching Floating-Point Numbers

When comparing floating-point numbers you might want to compare the actual value against the expectation within a given tolerance. That way actual and expected don't need to be an exact match &mdash; it just needs to be close enough.

We can do that by:

```java
final BigDecimal tolerance=BigDecimal.valueOf(0.001);
        assertThat(result,
        table()
        .withDefaultNumberTolerance(tolerance)
        .row(1.234)
        .row(3.1415)
        .matches());
```

In this example the tolerance is0.001.That means that the `ResultSetStructureMatcher` considers two numbers as equal if their absolute difference is smaller than the0.001.

However,this applies the tolerance check to all values that are being matched.

If you want to set the tolerance for the individual values,you can do so by using `cellMatcher`.

```java

final BigDecimal tolerance=BigDecimal.valueOf(0.001);
        assertThat(result,table()
        .row(CellMatcherFactory.cellMatcher(1.234,TypeMatchMode.STRICT,tolerance))
        .row(3.1415)
        .matches());

```

If you do not set any tolerance values, it uses by default `BigDecimal.ZERO`.

### Matching `Date` and `Timestamp`

When retrieving a `Date` or `Timestamp` from a database JDBC adapters use a `Calendar` to encode the date structure from the database into a the Unix-Timestamp stored in the `Date` or `Timestamp` object. Usually you set a calendar in the `getTimestamp(int columnIndex, Calendar cal)` method of the JDBC adapter. If you don't specify a timestamp there the JDBC adapter uses the Timezone of you test-computer. That's dangerous since it can be different for different developers or CI setups.

When you use this matcher it retrieves the column values from the result set, so you can not use the `getTimestamp` method yourself. For that reason you can configure a UTC calendar the matcher uses for reading `Date` and `Timestamp` values:

```java

assertThat(result,table()
        .row(new Timestamp(123456))
        .withUtcCalendar()
        .matches());
```

You can also specify a specific calendar using `withCalendar(Calendar)`.

If you don't specify a calendar the matcher uses the default calendar of your system (just like the JDBC driver). We strongly recommend not to do so!
It's however the default behaviour for backward compatibility. In that case the matcher displays a warning when reading a column without configured calendar.

### Display `Date` and `Timestamp`

Even so the Java classes `java.sql.Date` and `java.sql.Timestamp` represent a UTC value, their `toString()` method displays the date in the time-zone of the test system. That can easily lead to confusion. For that reason, this matcher instead prints them in UTC when the expected values mismatch.

Example:

```
Expected: a value equal to "2021-09-22T14:21:58Z"
     but:  was "2021-09-22T14:16:58Z"
```

You can see that it's UTC by the `Z`.