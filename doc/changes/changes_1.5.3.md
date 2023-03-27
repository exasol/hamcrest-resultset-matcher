# Matcher for SQL Result Sets 1.5.3, released 2023-03-27

Code name: Remove decommissioned maven.exasol.com repository

## Summary

This release removes the decommissioned `maven.exasol.com` repository to fix the build.

## Features

* #41: Removed decommissioned maven.exasol.com repository

## Dependency Updates

### Compile Dependency Updates

* Updated `com.exasol:error-reporting-java:0.4.1` to `1.0.1`

### Test Dependency Updates

* Updated `com.exasol:exasol-testcontainers:6.2.0` to `6.5.1`
* Updated `org.junit.jupiter:junit-jupiter-engine:5.9.0` to `5.9.2`
* Updated `org.junit.jupiter:junit-jupiter-params:5.9.0` to `5.9.2`
* Updated `org.testcontainers:jdbc:1.17.3` to `1.17.6`
* Updated `org.testcontainers:junit-jupiter:1.17.3` to `1.17.6`

### Plugin Dependency Updates

* Updated `com.exasol:error-code-crawler-maven-plugin:1.1.2` to `1.2.2`
* Updated `com.exasol:project-keeper-maven-plugin:2.6.2` to `2.9.6`
* Updated `io.github.zlika:reproducible-build-maven-plugin:0.15` to `0.16`
* Updated `org.apache.maven.plugins:maven-deploy-plugin:3.0.0-M1` to `3.1.0`
* Updated `org.apache.maven.plugins:maven-enforcer-plugin:3.1.0` to `3.2.1`
* Updated `org.apache.maven.plugins:maven-failsafe-plugin:3.0.0-M5` to `3.0.0-M8`
* Updated `org.apache.maven.plugins:maven-javadoc-plugin:3.4.0` to `3.4.1`
* Updated `org.apache.maven.plugins:maven-surefire-plugin:3.0.0-M5` to `3.0.0-M8`
* Updated `org.codehaus.mojo:flatten-maven-plugin:1.2.7` to `1.3.0`
* Updated `org.codehaus.mojo:versions-maven-plugin:2.10.0` to `2.14.2`
