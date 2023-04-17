# Matcher for SQL Result Sets 1.6.0, released 2023-04-17

Code name: Match in any order

## Summary

In this release we added the ability to match rows in any order.

This is very useful in case you have unordered results that you need to validate. It also makes sure that you are not forced to introduce artificial ordering in your production code where not required just to enable testing.

There are downsides to unordered matching too, that you should be aware of:

1. The algorithm uses quadratic time, also known as O(n²), so running it against large result sets comes with worse performance than the ordered counterpart. That being said, this library is intended for functional testing, and you will usually not have use cases where you formulate large expected result sets in your integration tests.
2. The diagnostic messages are less precise, since matching in any order introduces a lot of fuzziness. For example when the row count differs between expectation and actual result, additionally specifying the first mismatch makes no sense. In an ordered match it does.

## Features

* 16: Match rows in any order

## Dependency Updates

### Test Dependency Updates

* Updated `com.exasol:exasol-testcontainers:6.5.1` to `6.5.2`
* Updated `org.testcontainers:jdbc:1.17.6` to `1.18.0`
* Updated `org.testcontainers:junit-jupiter:1.17.6` to `1.18.0`

### Plugin Dependency Updates

* Updated `com.exasol:error-code-crawler-maven-plugin:1.2.2` to `1.2.3`
* Updated `com.exasol:project-keeper-maven-plugin:2.9.6` to `2.9.7`
* Updated `org.apache.maven.plugins:maven-compiler-plugin:3.10.1` to `3.11.0`
* Updated `org.apache.maven.plugins:maven-deploy-plugin:3.1.0` to `3.1.1`
* Updated `org.apache.maven.plugins:maven-enforcer-plugin:3.2.1` to `3.3.0`
* Updated `org.apache.maven.plugins:maven-failsafe-plugin:3.0.0-M8` to `3.0.0`
* Updated `org.apache.maven.plugins:maven-javadoc-plugin:3.4.1` to `3.5.0`
* Updated `org.apache.maven.plugins:maven-surefire-plugin:3.0.0-M8` to `3.0.0`
* Added `org.basepom.maven:duplicate-finder-maven-plugin:1.5.1`
* Updated `org.codehaus.mojo:flatten-maven-plugin:1.3.0` to `1.4.1`
* Updated `org.codehaus.mojo:versions-maven-plugin:2.14.2` to `2.15.0`
* Updated `org.jacoco:jacoco-maven-plugin:0.8.8` to `0.8.9`
