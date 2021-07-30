# Hamcrest Result Set Matcher.

[![Build Status](https://github.com/exasol/hamcrest-resultset-matcher/actions/workflows/ci-build.yml/badge.svg)](https://github.com/exasol/hamcrest-resultset-matcher/actions/workflows/ci-build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.exasol/hamcrest-resultset-matcher)](https://search.maven.org/artifact/com.exasol/hamcrest-resultset-matcher)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Ahamcrest-resultset-matcher&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.exasol%3Ahamcrest-resultset-matcher)

[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Ahamcrest-resultset-matcher&metric=security_rating)](https://sonarcloud.io/dashboard?id=com.exasol%3Ahamcrest-resultset-matcher)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Ahamcrest-resultset-matcher&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=com.exasol%3Ahamcrest-resultset-matcher)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Ahamcrest-resultset-matcher&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=com.exasol%3Ahamcrest-resultset-matcher)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Ahamcrest-resultset-matcher&metric=sqale_index)](https://sonarcloud.io/dashboard?id=com.exasol%3Ahamcrest-resultset-matcher)

[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Ahamcrest-resultset-matcher&metric=code_smells)](https://sonarcloud.io/dashboard?id=com.exasol%3Ahamcrest-resultset-matcher)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Ahamcrest-resultset-matcher&metric=coverage)](https://sonarcloud.io/dashboard?id=com.exasol%3Ahamcrest-resultset-matcher)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Ahamcrest-resultset-matcher&metric=duplicated_lines_density)](https://sonarcloud.io/dashboard?id=com.exasol%3Ahamcrest-resultset-matcher)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Ahamcrest-resultset-matcher&metric=ncloc)](https://sonarcloud.io/dashboard?id=com.exasol%3Ahamcrest-resultset-matcher)

# Overview

This project provides [Hamcrest matcher](http://hamcrest.org/JavaHamcrest/) that compares JDBC result set (`java.sql.ResultSet`) against each other or against Java structures.

# In a Nutshell

```java
import static com.exasol.matcher.ResultSetStructurMatcher.*;

class MyTest {
    @Test
    void testCustomerTableContents() {
        // Preparation: Create a JDBC statement and store the reference in variable 'statement'
        final ResulSet result = statement.executeQuery("SELECT * FROM CUSTOMERS");
        assertThat(result, table("INTEGER", "VARCHAR", "VARCHAR")
                .row(1, "JOHN", "DOE")
                .row(2, "JANE", "SMITH")
                .matches());
    }
}
```

## Features

* Match two JDBC result sets
* Match a JDBC result set against an object structure

## Customer Support

This is an open source project which is written by enthusiasts at Exasol and not officially supported. We will still try to help you as much as possible. So please create GitHub issue tickets when you want to request features or report bugs.

# Table of Contents

## Information for Users

* [User Guide](doc/user_guide/user_guide.md)
* [Changelog](doc/changes/changelog.md)
* [Dependencies](dependencies.md)
