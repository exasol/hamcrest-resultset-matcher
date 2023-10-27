# Matcher for SQL Result Sets 1.6.2, released 2023-10-27

Code name: Fix expectation with too many columns

## Summary

We fixed an issue where expecting more columns than are actually in the result set would throw an `ArrayIndexOutOfBoundsException`.

## Features

* #44: Fixed `ArrayIndexOutOfBoundsException` when expecting more columns than are in the result set.

## Dependency Updates

### Test Dependency Updates

* Updated `org.testcontainers:jdbc:1.19.0` to `1.19.1`
* Updated `org.testcontainers:junit-jupiter:1.19.0` to `1.19.1`

### Plugin Dependency Updates

* Updated `com.exasol:error-code-crawler-maven-plugin:1.3.0` to `1.3.1`
* Updated `com.exasol:project-keeper-maven-plugin:2.9.12` to `2.9.14`
* Updated `org.apache.maven.plugins:maven-enforcer-plugin:3.4.0` to `3.4.1`
* Updated `org.apache.maven.plugins:maven-javadoc-plugin:3.5.0` to `3.6.0`
* Updated `org.codehaus.mojo:versions-maven-plugin:2.16.0` to `2.16.1`
* Updated `org.jacoco:jacoco-maven-plugin:0.8.10` to `0.8.11`
* Updated `org.sonarsource.scanner.maven:sonar-maven-plugin:3.9.1.2184` to `3.10.0.2594`
