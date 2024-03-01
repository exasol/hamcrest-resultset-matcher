# Matcher for SQL Result Sets 1.6.5, released 2024-03-01

Code name: Fix CVE-2024-25710 and CVE-2024-26308 in test dependency `org.apache.commons:commons-compress`

## Summary

This release fixes vulnerabilities CVE-2024-25710 and CVE-2024-26308 in test dependency `org.apache.commons:commons-compress`.

## Security

* #36: Fixed CVE-2024-25710 and CVE-2024-26308 in test dependency `org.apache.commons:commons-compress`

## Dependency Updates

### Test Dependency Updates

* Updated `com.exasol:exasol-testcontainers:7.0.0` to `7.0.1`
* Updated `org.junit.jupiter:junit-jupiter-engine:5.10.1` to `5.10.2`
* Updated `org.junit.jupiter:junit-jupiter-params:5.10.1` to `5.10.2`
* Added `org.slf4j:slf4j-jdk14:2.0.12`
* Updated `org.testcontainers:jdbc:1.19.3` to `1.19.6`
* Updated `org.testcontainers:junit-jupiter:1.19.3` to `1.19.6`

### Plugin Dependency Updates

* Updated `com.exasol:error-code-crawler-maven-plugin:1.3.1` to `2.0.0`
* Updated `com.exasol:project-keeper-maven-plugin:3.0.0` to `4.1.0`
* Updated `org.apache.maven.plugins:maven-compiler-plugin:3.11.0` to `3.12.1`
* Updated `org.apache.maven.plugins:maven-failsafe-plugin:3.2.3` to `3.2.5`
* Updated `org.apache.maven.plugins:maven-surefire-plugin:3.2.3` to `3.2.5`
* Updated `org.codehaus.mojo:flatten-maven-plugin:1.5.0` to `1.6.0`
