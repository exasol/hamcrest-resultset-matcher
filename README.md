# Hamcrest Result Set Matcher.

[![Build Status](https://travis-ci.com/exasol/hamcrest-resultset-matcher.svg?branch=master)](https://travis-ci.org/exasol/hamcrest-resultset-matcher)
[![Maven Central](https://img.shields.io/maven-central/v/com.exasol/hamcrest-resultset-matcher)](https://search.maven.org/artifact/com.exasol/hamcrest-resultset-matcher)

SonarCloud results:

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

/...

@Test
void testCustomerTableContents() {
    // Preparation: Create a JDBC statement and store the reference in variable 'statement'
    
    final ResulSet result = statement.execute("SELECT * FROM CUSTOMERS");
    assertThat(result, table("INTEGER", "VARCHAR", "VARCHAR")
            .row(1, "JOHN", "DOE")
            .row(2, "JANE", "SMITH)
            .matches());
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

## Dependencies

### Run Time Dependencies

Running the Hamcrest ResultSet Matcher requires a Java Runtime version 11 or later.

### Build Time Dependencies

| Dependency                                                                          | Purpose                                                | License                       |
|-------------------------------------------------------------------------------------|--------------------------------------------------------|-------------------------------|
| [Java Hamcrest](http://hamcrest.org/JavaHamcrest/)                                  | Checking for conditions in code via matchers           | BSD License                   |


### Test Dependencies

| Dependency                                                                          | Purpose                                                | License                       |
|-------------------------------------------------------------------------------------|--------------------------------------------------------|-------------------------------|
| [Apache Derby](https://db.apache.org/derby/)                                        | Integration tests against real JDBC result sets        | Apache License 2.0            |
| [JUnit](https://junit.org/junit5)                                                   | Unit testing framework                                 | Eclipse Public License 1.0    |
| [Exasol Testcontainers][exasol-testcontainers]                                      | Exasol extension for the Testcontainers framework      | MIT License                   |
| [Testcontainers](https://www.testcontainers.org/)                                   | Container-based integration tests                      | MIT License                   |

### Maven Plug-ins

| Plug-in                                                                  | Purpose                                                | License                       |
|--------------------------------------------------------------------------|--------------------------------------------------------|-------------------------------|
| [Maven Compiler Plugin][maven-compiler-plugin]                           | Setting required Java version                          | Apache License 2.0            |
| [Maven Enforcer Plugin][maven-enforcer-plugin]                           | Controlling environment constants                      | Apache License 2.0            |
| [Maven GPG Plugin](https://maven.apache.org/plugins/maven-gpg-plugin/)   | Code signing                                           | Apache License 2.0            |
| [Maven Jacoco Plugin][maven-jacoco-plugin]                               | Code coverage metering                                 | Eclipse Public License 2.0    |
| [Maven Javadoc Plugin][maven-javadoc-plugin]                             | Creating a Javadoc JAR                                 | Apache License 2.0            |
| [Maven Source Plugin][maven-source-plugin]                               | Creating a source code JAR                             | Apache License 2.0            |
| [Maven Surefire Plugin][maven-surefire-plugin]                           | Unit testing                                           | Apache License 2.0            |
| [Maven Failsafe Plugin][maven-failsafe-plugin]                           | Integration testing                                    | Apache License 2.0            |
| [Sonatype OSS Index Maven Plugin][sonatype-oss-index-maven-plugin]       | Checking Dependencies Vulnerability                    | ASL2                          |
| [Versions Maven Plugin][versions-maven-plugin]                           | Checking if dependencies updates are available         | Apache License 2.0            |
  [Exasol Project Keeper][project-keeper]                                  | Unifying project structure                             | MIT License                   |

# License

This software is licensed under the [MIT license](LICENSE).

[maven-compiler-plugin]: https://maven.apache.org/plugins/maven-compiler-plugin/
[maven-enforcer-plugin]: http://maven.apache.org/enforcer/maven-enforcer-plugin/
[maven-jacoco-plugin]: https://www.eclemma.org/jacoco/trunk/doc/maven.html
[maven-javadoc-plugin]: https://maven.apache.org/plugins/maven-javadoc-plugin/
[maven-source-plugin]: https://maven.apache.org/plugins/maven-source-plugin/
[maven-surefire-plugin]: https://maven.apache.org/surefire/maven-surefire-plugin/
[sonatype-oss-index-maven-plugin]: https://sonatype.github.io/ossindex-maven/maven-plugin/
[versions-maven-plugin]: https://www.mojohaus.org/versions-maven-plugin/
[maven-failsafe-plugin]: https://maven.apache.org/surefire/maven-surefire-plugin/
[project-keeper]: https://github.com/exasol/project-keeper-maven-plugin
[exasol-testcontainers]: https://github.com/exasol/exasol-testcontainers