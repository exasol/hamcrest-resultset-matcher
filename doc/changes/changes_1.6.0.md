# Matcher for SQL Result Sets 1.6.0, released 2023-04-??

Code name: Match in any order

## Summary

In this release we added the ability to match rows in any order.

Note that this requires retrieving all columns before matching, so it will be slower and use more memory for large result sets than a simple match.

## Features

* 16: Match rows in any order

## Dependency Updates

### Test Dependency Updates

* Updated `com.exasol:exasol-testcontainers:6.5.1` to `6.5.2`
* Updated `org.apache.derby:derby:10.15.2.0` to `10.16.1.1`
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
