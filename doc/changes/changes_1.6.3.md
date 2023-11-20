# Matcher for SQL Result Sets 1.6.3, released 2023-11-20

Code name: Fix CVE-2023-4043 in test dependency `org.eclipse.parsson:parsson`

## Summary

This release fixes vulnerability CVE-2023-4043 in test dependency `org.eclipse.parsson:parsson`. The release also runs integration tests with Exasol 8.

## Security

* #48: Fixed CVE-2023-4043 in test dependency `org.eclipse.parsson:parsson`

## Dependency Updates

### Test Dependency Updates

* Updated `com.exasol:exasol-testcontainers:6.6.2` to `6.6.3`
* Updated `org.apache.derby:derby:10.15.2.0` to `10.16.1.1`
* Updated `org.junit.jupiter:junit-jupiter-engine:5.10.0` to `5.10.1`
* Updated `org.junit.jupiter:junit-jupiter-params:5.10.0` to `5.10.1`
* Updated `org.testcontainers:jdbc:1.19.1` to `1.19.2`
* Updated `org.testcontainers:junit-jupiter:1.19.1` to `1.19.2`

### Plugin Dependency Updates

* Updated `com.exasol:project-keeper-maven-plugin:2.9.14` to `2.9.16`
* Updated `org.apache.maven.plugins:maven-failsafe-plugin:3.1.2` to `3.2.2`
* Updated `org.apache.maven.plugins:maven-javadoc-plugin:3.6.0` to `3.6.2`
* Updated `org.apache.maven.plugins:maven-surefire-plugin:3.1.2` to `3.2.2`
